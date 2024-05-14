import { JsonRpc } from 'jsonrpc';
import { LitElement, html, css } from 'lit';
import {ref, createRef} from 'lit/directives/ref.js';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import './bpmnjs/zeebe-bpmn-diagram.js';
import '@vaadin/tabs';
import '@vaadin/grid';
import '@vaadin/tabsheet';
import '@vaadin/form-layout';
import '@vaadin/text-field';
import './components/zeebe-table.js';
import './components/zeebe-instance-cancel-dialog.js'
import './components/zeebe-variable-create-dialog.js'
import './components/zeebe-variable-edit-dialog.js'
import './components/zeebe-variable-history-dialog.js'

export class ZeebeInstance extends LitElement {

    static styles = css`
        .link > input {
            cursor: pointer;
            color: var(--quarkus-blue);
        }
        .flex-auto {
            flex: 1 1 auto;
        }
    `;

    _instanceCancelDialogRef = createRef();
    _variableCreateDialogRef = createRef();
    _variableEditDialogRef = createRef();
    _variableHistoryDialogRef = createRef();

    static properties = {
        _item: {state: true},
        context: {},
        navigation: {},
    };

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._fetchData();

        this._observer = this.jsonRpc.notifications().onNext(response => {
            this._fetchData();
            if (response.result.event === 'PROCESS_INSTANCE') {
                if (response.result.data.processInstanceKey === this._item.item.id) {
                    this._fetchData();
                }
            }
        });
    }

    render() {
        if (this._item) {
            return this._body();
        } else {
            return html`<p style="position: relative; overflow: hidden; width: 100%; height: 100%;"></p>`
        }
    }

    detailsColumn = [{ minWidth: 0, columns: 1 },{ minWidth: '600px', columns: 2 }, { minWidth: '1280px', columns: 3 }];

    _body() {
        return html`
            <zeebe-bpmn-diagram id="diagram" .xml="${this._item.xml}" .data=${this._item.diagram}></zeebe-bpmn-diagram>
            <vaadin-tabsheet>
                <vaadin-tabs slot="tabs">
                    <vaadin-tab id="process-info" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:circle-info"></vaadin-icon>
                        <span>Details</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-variables" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Variables</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-audit" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Audit log</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-incidents" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Incidents</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-jobs" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Jobs</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-user-tasks" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>User tasks</span>
                    </vaadin-tab>                    
                    <vaadin-tab id="process-messages" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:envelope"></vaadin-icon>
                        <span>Messages</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-escalation" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:envelope"></vaadin-icon>
                        <span>Escalation</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-timers" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Timers</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-called-instances" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Called instances</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-errors" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Errors</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-modify" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Modify</span>
                    </vaadin-tab>                    
                </vaadin-tabs>

                <div tab="process-info">
                    <vaadin-horizontal-layout theme="spacing padding"  style="align-items: stretch">
                        <div class="flex-auto"></div>
                        <vaadin-button theme="primary error" style="align-self: end" ?disabled=${!this._item.active}
                                       @click=${() => this._instanceCancelDialogRef.value.open(this._item.item.id)}>
                            <vaadin-icon slot="prefix" icon="font-awesome-solid:ban"></vaadin-icon>
                            Cancel
                        </vaadin-button>
                    </vaadin-horizontal-layout>
                    
                    <vaadin-form-layout .responsiveSteps="${this.detailsColumn}">
                        <vaadin-text-field readonly label="Key" value="${this._item.item.id}"></vaadin-text-field>
                        <vaadin-text-field readonly class="link" label="Process definition key"
                                           value="${this._item.item.record.value.processDefinitionKey}"
                                           @click=${() => this.navigation({ nav: "process", id: this._item.item.record.value.processDefinitionKey})}>
                        </vaadin-text-field>                        
                        <vaadin-text-field readonly label="BPMN process id" value="${this._item.item.record.value.bpmnProcessId}"></vaadin-text-field>
                        <vaadin-text-field readonly label="State" value="${this._item.item.data.state}"></vaadin-text-field>
                        <vaadin-text-field readonly label="Start time" value="${this._item.item.data.start}"></vaadin-text-field>
                        <vaadin-text-field readonly label="End time" value="${this._item.item.data.end}"></vaadin-text-field>
                        <vaadin-text-field readonly label="Version" value="${this._item.item.record.value.version}"></vaadin-text-field>
                    </vaadin-form-layout>
                    <zeebe-instance-cancel-dialog ${ref(this._instanceCancelDialogRef)} id="process-instance-cancel-dialog" .context=${this.context}></zeebe-instance-cancel-dialog>
                </div>
                <div tab="process-variables">
                    <zeebe-table id="instance-variables-table" .items=${this._item.variables}>
                        <vaadin-button slot="toolbar" theme="primary" style="align-self: end" @click=${() => this._variableCreateDialogRef.value.open(this._item.item.id, this._item.activeScopes)}
                                       ?disabled=${!this._item.active}>
                            <vaadin-icon slot="prefix" icon="font-awesome-solid:play"></vaadin-icon>
                            Create variable
                        </vaadin-button>
                        
                        <vaadin-grid-column header="Scope Key" path="scopeKey" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Element Id" path="elementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Name" path="name" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Value" path="value" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="time" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Actions" resizable ${columnBodyRenderer(this._variablesActionRenderer, [])}></vaadin-grid-column>
                    </zeebe-table>
                    <zeebe-variable-create-dialog ${ref(this._variableCreateDialogRef)} id="instance-variable-create-dialog" .context=${this.context}></zeebe-variable-create-dialog>
                    <zeebe-variable-edit-dialog ${ref(this._variableEditDialogRef)} id="instance-variable-edit-dialog" .context=${this.context}></zeebe-variable-edit-dialog>
                    <zeebe-variable-history-dialog ${ref(this._variableHistoryDialogRef)} id="instance-variable-history-dialog" .context=${this.context}></zeebe-variable-history-dialog>
                </div>
                <div tab="process-audit">2 This is the Dashboard tab content</div>
                <div tab="process-incidents">2 This is the Dashboard tab content</div>
                <div tab="process-jobs">4 This is the Dashboard tab content</div>
                <div tab="process-user-tasks">5 This is the Dashboard tab content</div>
                <div tab="process-messages">3 This is the Dashboard tab content</div>
                <div tab="process-escalation">5 This is the Dashboard tab content</div>
                <div tab="process-timers">5 This is the Dashboard tab content</div>
                <div tab="process-called-instances">5 This is the Dashboard tab content</div>
                <div tab="process-errors">5 This is the Dashboard tab content</div>
                <div tab="process-modify">5 This is the Dashboard tab content</div>

            </vaadin-tabsheet>        
        `;
    }

    _variablesActionRenderer(item) {
        return html`
            <vaadin-icon icon="font-awesome-regular:file-lines" style="color: var(--lumo-primary-text-color)"
                         title="Show variable history log"
                         @click=${() => this._variableHistoryDialogRef.value.open(item.variables)}
            ></vaadin-icon>            
            <vaadin-icon icon="font-awesome-regular:pen-to-square" style="color: var(--lumo-primary-text-color)"
                         title="Edit variable"
                         ?hidden=${!this._item.active}
                         @click=${() => this._variableEditDialogRef.value.open(item.name, item.elementId, item.value)}
            ></vaadin-icon>
        `;
    }

    _fetchData() {
        this.jsonRpc.instance({id: this.context.id})
            .then(itemResponse => {
                let tmp = itemResponse.result;
                tmp.variables = tmp.variables.map((item) => ({
                    ...item,
                    searchTerms: `${item.scopeKey} ${item.name} ${item.elementId}`,
                }));
                tmp.jobs = tmp.jobs.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.type} ${item.id} ${item.record.value.processInstanceKey}`,
                }));
                this._item = tmp;
            });
    }
}

customElements.define('zeebe-instance', ZeebeInstance);