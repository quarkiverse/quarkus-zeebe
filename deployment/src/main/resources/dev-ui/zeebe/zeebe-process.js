import { JsonRpc } from 'jsonrpc';
import { LitElement, html} from 'lit';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import {ref, createRef} from 'lit/directives/ref.js';
import { diagramId } from './components/zeebe-utils.js';
import './bpmnjs/zeebe-bpmn-diagram.js';
import '@vaadin/tabs';
import '@vaadin/tabsheet';
import '@vaadin/text-area';
import '@vaadin/form-layout';
import '@vaadin/text-field';
import './components/zeebe-table.js';
import './components/zeebe-send-message-dialog.js';
import './components/zeebe-send-signal-dialog.js';
import './components/zeebe-instance-create-dialog.js';

export class ZeebeProcess extends LitElement {

    static properties = {
        _item: {state: true},
        context: {},
        navigation: {},
    };

    _diagram = createRef();
    _sendMessageDialogRef = createRef();
    _sendSignalDialogRef = createRef();
    _createInstanceDialogRef = createRef();

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._fetchData();

        this._observer = this.jsonRpc.notifications().onNext(response => {
            if (response.result.event === 'PROCESS_INSTANCE') {
                if (this._item.item.id === response.result.data.processDefinitionKey) {
                    this._fetchData();
                }
            }
            if (response.result.event === 'PROCESS') {
                this._fetchData();
            }
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
            <zeebe-bpmn-diagram id="process-diagram" ${ref(this._diagram)} .xml="${this._item.xml}" .data=${this._item.diagram}></zeebe-bpmn-diagram>
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
                    <zeebe-table id="process-instances-table" .items=${this._item.instances}>
                        <vaadin-button slot="toolbar" theme="primary" style="align-self: end" @click=${() => this._createInstanceDialogRef.value.open(this._item.item.record.value.processDefinitionKey)}>
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
                    <zeebe-instance-create-dialog ${ref(this._createInstanceDialogRef)} .context=${this.context}></zeebe-instance-create-dialog>
                </div>
                <div tab="process-messages">
                    <zeebe-table id="process-messages-table" .items=${this._item.messages}>
                        <vaadin-grid-column ${columnBodyRenderer(this._diagramMessageIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
                        <vaadin-grid-column header="Element Id" path="record.value.startEventId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Message name" path="record.value.messageName" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="record.intent" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="data.time" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Actions" ${columnBodyRenderer(this._messageSubscriptionActionRenderer, [])}></vaadin-grid-column>
                    </zeebe-table>
                    <zeebe-send-message-dialog ${ref(this._sendMessageDialogRef)} id="process-send-message-dialog" .context=${this.context}></zeebe-send-message-dialog>
                </div>
                <div tab="process-signals">
                    <zeebe-table id="process-messages-signals" .items=${this._item.signals}>
                        <vaadin-grid-column ${columnBodyRenderer(this._diagramSignalIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
                        <vaadin-grid-column header="Catch Event Id" path="record.value.catchEventId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Catch Event Instance Key" path="record.value.catchEventInstanceKey" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Signal Name" path="record.value.signalName"></vaadin-grid-column>
                        <vaadin-grid-column header="Status" path="record.intent"></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="data.time"></vaadin-grid-column>
                        <vaadin-grid-column header="Actions" ${columnBodyRenderer(this._signalSubscriptionActionRenderer, [])}></vaadin-grid-column>
                    </zeebe-table>
                    <zeebe-send-signal-dialog ${ref(this._sendSignalDialogRef)} id="process-send-signal-dialog" .context=${this.context}></zeebe-send-signal-dialog>
                </div>
                <div tab="process-timers">
                    <zeebe-table id="process-timers-table" .items=${this._item.timers}>
                        <vaadin-grid-column ${columnBodyRenderer(this._diagramTimerIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
                        <vaadin-grid-column header="Element Id" path="record.value.targetElementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Due Date" path="data.dueDate" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Repetitions" path="record.value.repetitions"></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="record.intent"></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="data.time"></vaadin-grid-column>
                    </zeebe-table>
                </div>

            </vaadin-tabsheet>            
        `;
    }

    _diagramSignalIdRenderer(item) {
        return diagramId(this._diagram, item.record.value.catchEventId);
    }

    _diagramTimerIdRenderer(item) {
        return diagramId(this._diagram, item.record.value.targetElementId);
    }

    _diagramMessageIdRenderer(item) {
        return diagramId(this._diagram, item.record.value.startEventId);
    }

    _instanceKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "instance", id: item.id })}>${item.id}</a>
        `;
    }

    _signalSubscriptionActionRenderer(item) {
        return html`
            <vaadin-icon slot="prefix" icon="font-awesome-regular:envelope" style="color: var(--lumo-primary-text-color)"
                         title="Broadcast signal"
                         @click=${() => this._sendSignalDialogRef.value.open(item.record.value.signalName)}
            ></vaadin-icon>
        `;
    }

    _messageSubscriptionActionRenderer(item) {
        return html`
                <vaadin-icon icon="font-awesome-regular:envelope" style="color: var(--lumo-primary-text-color)"
                             title="Send message"
                             @click=${() => this._sendMessageDialogRef.value.open(item.record.value.messageName)}
                ></vaadin-icon>
        `;
    }

    _fetchData() {
        this.jsonRpc.process({id: this.context.id})
            .then(itemResponse => {
                let tmp = itemResponse.result;
                tmp.instances = tmp.instances.map((item) => ({
                    ...item,
                    searchTerms: `${item.id} ${item.record.value.bpmnProcessId} ${item.record.value.processDefinitionKey}`,
                }));
                tmp.messages = tmp.messages.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.messageName} ${item.record.value.startEventId}`,
                }));
                tmp.signals = tmp.signals.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.signalName} ${item.record.value.catchEventId}`,
                }));
                tmp.timers = tmp.timers.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.targetElementId}`,
                }));
                this._item = tmp;
            });
    }

}

customElements.define('zeebe-process', ZeebeProcess);