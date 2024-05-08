import { JsonRpc } from 'jsonrpc';
import { LitElement, html} from 'lit';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import { dialogRenderer, dialogHeaderRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';
import './bpmnjs/zeebe-bpmn-diagram.js';
import '@vaadin/tabs';
import '@vaadin/tabsheet';
import '@vaadin/text-area';
import '@vaadin/form-layout';
import '@vaadin/text-field';
import './components/zeebe-table.js';

export class ZeebeProcess extends LitElement {

    static properties = {
        _item: {state: true},
        context: {},
        navigation: {},
        _createInstanceOpened: { state: true},
        _createInstanceVariables: { state: true },
    };

    connectedCallback() {
        super.connectedCallback();
        this._createInstanceOpened = false;
        this._createInstanceVariables = null;
        this.jsonRpc = new JsonRpc(this.context.extension);
        this.jsonRpc.process({id: this.context.id})
            .then(itemResponse => {
                this._item = itemResponse.result;
                this._item.instances = this._item.instances.map((item) => ({
                    ...item,
                    searchTerms: `${item.id} ${item.record.value.bpmnProcessId} ${item.record.value.processDefinitionKey}`,
                }));
            });

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
                        <vaadin-button slot="toolbar" theme="primary" style="align-self: end" @click=${() => this._openCreateProcessInstanceDialog()}>
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
                    <vaadin-dialog header-title="Create new process instance" .opened=${this._createInstanceOpened}
                           @opened-changed=${(e) => {this._createInstanceOpened = e.detail.value;}}
                           ${dialogHeaderRenderer(() => html`<vaadin-icon @click=${() => {this._createInstanceOpened = false}} icon="font-awesome-solid:xmark"></vaadin-icon>`, [] )}
                           ${dialogRenderer(() => html`
                               <vaadin-text-area 
                                       id="create-instance-text-area"
                                       style="width:100%; min-width: 400px; min-height: 200px; max-height: 400px;" 
                                       helper-text="Variables in JSON format" 
                                       label="Variables"
                                       placeholder='{"variable1":"value"}'
                                       value="${this._createInstanceVariables}"
                                       @value-changed=${(e) => {this._createInstanceVariables = e.detail.value;}}
                                       >
                               </vaadin-text-area>
                           `, [])}
                           ${dialogFooterRenderer(() => html`
                                <vaadin-button theme="primary" @click=${() => this._createProcessInstance()} style="margin-right: auto;">
                                    Create</vaadin-button>`, [])}
                    >
                    </vaadin-dialog>
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

    _openCreateProcessInstanceDialog() {
        this._createInstanceVariables = '';
        this._createInstanceOpened = true;
    }

    _createProcessInstance() {
        let variables = {};
        if (this._createInstanceVariables) {
            variables = JSON.parse(this._createInstanceVariables);
        }

        console.log(variables);
        this.jsonRpc.createProcessInstance({processDefinitionKey: this._item.item.id, variables: variables})
            .then(itemResponse => {
                document.getElementById("create-instance-text-area").value='';
                console.log(itemResponse);
                this._createInstanceOpened = false
            });

    }
}

customElements.define('zeebe-process', ZeebeProcess);