import { JsonRpc } from 'jsonrpc';
import { LitElement, html} from 'lit';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import {ref, createRef} from 'lit/directives/ref.js';
import './bpmnjs/zeebe-bpmn-diagram.js';
import '@vaadin/tabs';
import '@vaadin/tabsheet';
import '@vaadin/text-area';
import '@vaadin/form-layout';
import '@vaadin/text-field';
import './components/zeebe-table.js';
import './components/zeebe-dialog.js';
import { notifier } from 'notifier';

export class ZeebeProcess extends LitElement {

    static properties = {
        _item: {state: true},
        context: {},
        navigation: {},
        _createInstanceOpened: { state: true},
        _createInstanceVariables: { state: true },
    };

    _createInstanceTextAreaRef = createRef();

    connectedCallback() {
        super.connectedCallback();
        this._createInstanceOpened = false;
        this._createInstanceVariables = null;
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._fetchData();

        this._observer = this.jsonRpc.notifications().onNext(response => {
            this._updateData(response);
        });
    }

    disconnectedCallback() {
        this._observer.cancel();
        super.disconnectedCallback()
    }

    render() {
        if (this._item) {
            return this._body();
        } else {
            return html`<p style="position: relative; overflow: hidden; width: 100%; height: 100%;"></p>`
        }
    }

    _body() {
        return html`
            <zeebe-bpmn-diagram id="diagram" .xml="${this._item.xml}" .data=${this._item.diagram}></zeebe-bpmn-diagram>
            <vaadin-tabsheet>
                <vaadin-tabs slot="tabs">
                    <vaadin-tab id="process-info" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:circle-info"></vaadin-icon>
                        <span>Details</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instances" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Instances</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-messages" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:envelope"></vaadin-icon>
                        <span>Message subscriptions</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-signals" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:envelope"></vaadin-icon>
                        <span>Signal subscriptions</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-timers" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Timers</span>
                    </vaadin-tab>
                </vaadin-tabs>

                <div tab="process-info">
                    <vaadin-form-layout>
                        <vaadin-text-field readonly label="Key" value="${this._item.item.id}"></vaadin-text-field>
                        <vaadin-text-field readonly label="BPMN process id" value="${this._item.item.record.value.bpmnProcessId}"></vaadin-text-field>
                        <vaadin-text-field readonly label="Version" value="${this._item.item.record.value.version}"></vaadin-text-field>
                        <vaadin-text-field readonly label="Deploy time" value="${this._item.item.data.time}"></vaadin-text-field>
                    </vaadin-form-layout>
                </div>
                <div tab="process-instances">
                    <zeebe-table .items=${this._item.instances}>
                        <vaadin-button slot="toolbar" theme="primary" style="align-self: end" @click=${() => this._createInstanceOpened = true}>
                            <vaadin-icon slot="prefix" icon="font-awesome-solid:play"></vaadin-icon>
                            Create instance
                        </vaadin-button>
                        
                        <vaadin-grid-column header="Process Instance Key" ${columnBodyRenderer(this._instanceKeyRenderer, [])} resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Process Id" path="record.value.bpmnProcessId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Process key" path="record.value.processDefinitionKey"></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="data.state"></vaadin-grid-column>
                        <vaadin-grid-column header="Start time" path="data.start" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="End time" path="data.end" resizable></vaadin-grid-column>
                    </zeebe-table>
                    <zeebe-dialog id="dialog" title="Create new process instance" titleAction="Create" .opened=${this._createInstanceOpened}
                            .renderDialog=${() => this._createInstanceRenderDialog()}
                            .actionDialog=${() => this._createInstanceAction()}      
                            .closeDialog=${() => this._createInstanceClose()}
                        >
                    </zeebe-dialog>
                </div>
                <div tab="process-messages">
                    <zeebe-table .items=${this._item.messages}>
                        <vaadin-grid-column header="Id" path="id" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Message name" path="record.value.messageName" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="record.intent"></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="data.time"></vaadin-grid-column>
                    </zeebe-table>
                </div>
                <div tab="process-signals">
                    <zeebe-table .items=${this._item.signals}>
                        <vaadin-grid-column header="Catch Event Id" path="id" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Catch Event Instance Key" path="record.value.messageName" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Signal Name" path="record.intent"></vaadin-grid-column>
                        <vaadin-grid-column header="Status" path="record.intent"></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="data.time"></vaadin-grid-column>
                        <vaadin-grid-column header="Actions" path="data.time"></vaadin-grid-column>
                    </zeebe-table>                    
                </div>
                <div tab="process-timers">
                    <zeebe-table .items=${this._item.timers}>
                        <vaadin-grid-column header="Element Id" path="id" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Due Date" path="record.value.messageName" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Repetitions" path="record.intent"></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="record.intent"></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="data.time"></vaadin-grid-column>
                    </zeebe-table>
                </div>

            </vaadin-tabsheet>            
        `;
    }

    _instanceKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "instance", id: item.id })}>${item.id}</a>
        `;
    }

    _createInstanceRenderDialog() {
        return html`
            <vaadin-text-area
                    ${ref(this._createInstanceTextAreaRef)}
                    style="width:100%; min-width: 400px; min-height: 200px; max-height: 400px;"
                    helper-text="Variables in JSON format"
                    label="Variables"
                    placeholder='{"variable1":"value"}'
                    value="${this._createInstanceVariables}"
                    @value-changed=${(e) => {this._createInstanceVariables = e.detail.value;}}
            >
            </vaadin-text-area>
        `;
    }

    _createInstanceClose() {
        this._createInstanceTextAreaRef.value.value = '';
        this._createInstanceOpened = false
    }

    _createInstanceAction() {
        let variables = {};
        if (this._createInstanceVariables) {
            try {
                variables = JSON.parse(this._createInstanceVariables);
            } catch (e) {
                this._error(e.message);
                return
            }
        }
        this.jsonRpc.createProcessInstance({processDefinitionKey: this._item.item.id, variables: variables})
            .then(response => {
                console.log(response);
                this._createInstanceClose();
            })
            .catch(e => {
                console.log(e);
                this._error('Create process instance error: ' + e.error.code + ' message: ' + e.error.message)
            });
    }

    _error(msg){
        notifier.showErrorMessage(msg, null);
    }

    _fetchData() {
        this.jsonRpc.process({id: this.context.id})
            .then(itemResponse => {
                this._item = itemResponse.result;
                this._item.instances = this._item.instances.map((item) => ({
                    ...item,
                    searchTerms: `${item.id} ${item.record.value.bpmnProcessId} ${item.record.value.processDefinitionKey}`,
                }));
            });
    }

    _updateData(response) {
        if (response.result.type === 'PROCESS_INSTANCE') {
            if (this._item.item.id === response.result.data.processDefinitionKey) {
                this._fetchData();
            }
        }
    }
}

customElements.define('zeebe-process', ZeebeProcess);