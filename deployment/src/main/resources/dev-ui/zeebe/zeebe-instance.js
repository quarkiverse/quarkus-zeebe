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
import './components/zeebe-incident-details-dialog.js'
import './components/zeebe-incident-resolve-dialog.js'

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
    _incidentDetailsDialogRef = createRef();
    _incidentResolveDialogRef = createRef();

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
                    <vaadin-tab id="process-instance-info" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:circle-info"></vaadin-icon>
                        <span>Details</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-variables" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Variables</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-audit" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Audit log</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-incidents" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Incidents</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-jobs" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Jobs</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-user-tasks" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>User tasks</span>
                    </vaadin-tab>                    
                    <vaadin-tab id="process-instance-messages" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:envelope"></vaadin-icon>
                        <span>Messages</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-escalation" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:envelope"></vaadin-icon>
                        <span>Escalation</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-timers" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Timers</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-called-instances" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Called instances</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-errors" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Errors</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-modify" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Modify</span>
                    </vaadin-tab>                    
                </vaadin-tabs>

                <div tab="process-instance-info">
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
                <div tab="process-instance-variables">
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
                <div tab="process-instance-audit">
                    <zeebe-table id="instance-variables-table" .items=${this._item.auditLogEntries}>
                        <vaadin-grid-column header="Element Id" path="item.record.value.elementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Element Key" path="item.record.key" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Flow Scope Key" path="item.record.value.flowScopeKey" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="item.record.intent" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Name" path="elementName" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Type" path="item.record.value.bpmnElementType" resizable></vaadin-grid-column>
                    </zeebe-table>
                </div>
                <div tab="process-instance-incidents">
                    <zeebe-table id="instance-incidents-table" .items=${this._item.incidents}>
                        <vaadin-grid-column header="Element Id" path="item.record.value.elementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Incident Key" path="item.record.key" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Type" path="item.record.value.errorType" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Message" path="item.record.value.errorMessage" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="item.record.intent" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Create" path="item.data.created" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Resolved" path="item.data.resolved" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Actions" ${columnBodyRenderer(this._incidentsActionRenderer, [])}></vaadin-grid-column>
                    </zeebe-table>
                    <zeebe-incident-details-dialog ${ref(this._incidentDetailsDialogRef)} id="instance-instance-details-dialog" .context=${this.context}></zeebe-incident-details-dialog>
                    <zeebe-incident-resolve-dialog ${ref(this._incidentResolveDialogRef)} id="instance-instance-resolve-dialog" .context=${this.context}></zeebe-incident-resolve-dialog>
                </div>
                <div tab="process-instance-jobs">
                    <zeebe-table id="instance-incidents-table" .items=${this._item.jobs}>
                        <vaadin-grid-column header="Element Id" path="record.value.elementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Job Key" path="record.key" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Job Type" path="record.value.type" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Retries" path="record.value.retries" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Error Code" path="record.value.errorCode" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Error Message" path="record.value.errorMessage" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="record.intent" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="data.time" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Actions" ${columnBodyRenderer(this._jobsActionRenderer, [])} auto-width></vaadin-grid-column>
                    </zeebe-table>
                </div>
                <div tab="process-instance-user-tasks">5 This is the Dashboard tab content</div>
                <div tab="process-instance-messages">3 This is the Dashboard tab content</div>
                <div tab="process-instance-escalation">5 This is the Dashboard tab content</div>
                <div tab="process-instance-timers">5 This is the Dashboard tab content</div>
                <div tab="process-instance-called-instances">5 This is the Dashboard tab content</div>
                <div tab="process-instance-errors">5 This is the Dashboard tab content</div>
                <div tab="process-instance-modify">5 This is the Dashboard tab content</div>

            </vaadin-tabsheet>        
        `;
    }

    _jobsActionRenderer(item) {
        return html`
            <vaadin-icon icon="font-awesome-regular:circle-check" style="color: var(--lumo-primary-text-color)"
                         title="Complete job"
                         ?hidden=${!item.active || !this._item.active}
            ></vaadin-icon>
            <vaadin-icon icon="font-awesome-regular:circle-xmark" style="color: var(--lumo-primary-text-color)"
                         title="Fail job"
                         ?hidden=${!item.active || !this._item.active}
            ></vaadin-icon>
            <vaadin-icon icon="font-awesome-regular:circle-dot" style="color: var(--lumo-primary-text-color)"
                         title="Throw error"
                         ?hidden=${!item.active || !this._item.active}
            ></vaadin-icon>
            <vaadin-icon icon="font-awesome-solid:rotate" style="color: var(--lumo-primary-text-color)"
                         title="Update retries"
                         ?hidden=${!this._item.active}
            ></vaadin-icon>
        `;
    }

    _incidentsActionRenderer(item) {
        return html`
            <vaadin-icon icon="font-awesome-regular:file-lines" style="color: var(--lumo-primary-text-color)"
                         title="Show details"
                         @click=${() => this._incidentDetailsDialogRef.value.open(item)}
            ></vaadin-icon>            
            <vaadin-icon icon="font-awesome-regular:circle-play" style="color: var(--lumo-primary-text-color)"
                         title="Resolve incident"
                         ?hidden=${!this._item.active || item.item.data.resolved !== ""}
                         @click=${() => this._incidentResolveDialogRef.value.open(item)}
            ></vaadin-icon>
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
                tmp.incidents = tmp.incidents.map((item) => ({
                    ...item,
                    searchTerms: `${item.item.record.key} ${item.item.record.value.errorMessage}`,
                }));
                tmp.jobs = tmp.jobs.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.type} ${item.id} ${item.record.value.processInstanceKey}`,
                }));
                tmp.auditLogEntries = tmp.auditLogEntries.map((item) => ({
                    ...item,
                    searchTerms: `${item.elementName} ${item.item.record.value.elementId} ${item.item.record.value.bpmnElementType}`
                }));
                tmp.jobs = tmp.jobs.map((item) => ({
                    ...item,
                    active: item.record.value.retries > 0 && (item.record.intent == 'CREATED' || item.record.intent == 'FAILED'
                                    || item.record.intent == 'TIMED_OUT' || item.record.intent == 'RETRIES_UPDATED'),
                    searchTerms: `${item.record.intent} ${item.record.value.jobType} ${item.record.value.jobKey}`
                }));
                console.log(tmp.jobs);
                this._item = tmp;
            });
    }
}

customElements.define('zeebe-instance', ZeebeInstance);