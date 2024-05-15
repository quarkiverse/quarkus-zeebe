import { JsonRpc } from 'jsonrpc';
import { LitElement, html, css } from 'lit';
import {ref, createRef} from 'lit/directives/ref.js';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import { diagramId } from './components/zeebe-utils.js';
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
import './components/zeebe-job-complete-dialog.js'
import './components/zeebe-job-fail-dialog.js'
import './components/zeebe-job-throw-error-dialog.js'
import './components/zeebe-job-retries-dialog.js'
import './components/zeebe-user-task-info-dialog.js';
import './components/zeebe-user-task-complete-dialog.js';
import './components/zeebe-send-message-dialog.js';
import './components/zeebe-send-signal-dialog.js';

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

    _diagram = createRef();
    _instanceCancelDialogRef = createRef();
    _variableCreateDialogRef = createRef();
    _variableEditDialogRef = createRef();
    _variableHistoryDialogRef = createRef();
    _incidentDetailsDialogRef = createRef();
    _incidentResolveDialogRef = createRef();
    _jobCompleteDialogRef = createRef();
    _jobFailDialogRef = createRef();
    _jobThrowErrorDialogRef = createRef();
    _jobRetriesDialogRef = createRef();
    _userTaskCompleteDialogRef = createRef();
    _userTaskInfoDialogRef = createRef();
    _sendMessageDialogRef = createRef();

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
            <zeebe-bpmn-diagram id="instance-diagram" ${ref(this._diagram)} .xml="${this._item.xml}" .data=${this._item.diagram}></zeebe-bpmn-diagram>
            <vaadin-tabsheet>
                <vaadin-tabs slot="tabs">
                    <vaadin-tab id="process-instance-info" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:circle-info"></vaadin-icon>
                        <span>Details</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-variables" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:database"></vaadin-icon>
                        <span>Variables</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-audit" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:wave-square"></vaadin-icon>
                        <span>Audit log</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-incidents" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:triangle-exclamation"></vaadin-icon>
                        <span>Incidents</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-jobs" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:person-digging"></vaadin-icon>
                        <span>Jobs</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-user-tasks" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:user"></vaadin-icon>
                        <span>User tasks</span>
                    </vaadin-tab>                    
                    <vaadin-tab id="process-instance-messages" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:envelope"></vaadin-icon>
                        <span>Messages</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-escalation" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:chart-line"></vaadin-icon>
                        <span>Escalation</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-timers" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Timers</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-called-instances" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:arrow-right-from-bracket"></vaadin-icon>
                        <span>Called instances</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instance-errors" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:bug"></vaadin-icon>
                        <span>Errors</span>
                    </vaadin-tab>
<!--                    <vaadin-tab id="process-instance-modify" theme="icon">-->
<!--                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>-->
<!--                        <span>Modify</span>-->
<!--                    </vaadin-tab>                    -->
                </vaadin-tabs>

                <div tab="process-instance-info">
                    <vaadin-horizontal-layout theme="spacing"  style="align-items: stretch">
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
                         <vaadin-text-field readonly class="link" label="Parent process instance key" 
                                       value="${this._item.parent == null ? '' : this._item.parent.record.value.processInstanceKey}"
                                       @click=${() => this.navigation({ nav: "instance", id: this._item.parent.record.value.processInstanceKey})}>
                        </vaadin-text-field>
                        <vaadin-text-field readonly class="link" label="Parent process definition key" 
                                           value="${this._item.parent == null ? '' : this._item.parent.record.value.processDefinitionKey}"
                                           @click=${() => this.navigation({ nav: "process", id: this._item.parent.record.value.processDefinitionKey})}>
                        </vaadin-text-field>
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
                        <vaadin-grid-column ${columnBodyRenderer(this._rootElementIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
                        <vaadin-grid-column header="Element Id" path="elementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Scope Key" path="scopeKey" resizable></vaadin-grid-column>
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
                    <zeebe-table id="instance-audit-table" .items=${this._item.auditLogEntries}>
                        <vaadin-grid-column ${columnBodyRenderer(this._itemElementIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
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
                        <vaadin-grid-column ${columnBodyRenderer(this._itemElementIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
                        <vaadin-grid-column header="Element Id" path="item.record.value.elementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Incident Key" path="item.record.key" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Type" path="item.record.value.errorType" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Message" path="item.record.value.errorMessage" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="item.record.intent" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Create" path="item.data.created" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Resolved" path="item.data.resolved" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Actions" ${columnBodyRenderer(this._incidentsActionRenderer, [])}></vaadin-grid-column>
                    </zeebe-table>
                    <zeebe-incident-details-dialog ${ref(this._incidentDetailsDialogRef)} id="instance-incident-details-dialog" .context=${this.context}></zeebe-incident-details-dialog>
                    <zeebe-incident-resolve-dialog ${ref(this._incidentResolveDialogRef)} id="instance-incident-resolve-dialog" .context=${this.context}></zeebe-incident-resolve-dialog>
                </div>
                <div tab="process-instance-jobs">
                    <zeebe-table id="instance-incidents-table" .items=${this._item.jobs}>
                        <vaadin-grid-column ${columnBodyRenderer(this._elementIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
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
                    <zeebe-job-complete-dialog ${ref(this._jobCompleteDialogRef)} id="instance-job-complete-dialog" .context=${this.context}></zeebe-job-complete-dialog>
                    <zeebe-job-fail-dialog ${ref(this._jobFailDialogRef)} id="instance-job-fail-dialog" .context=${this.context}></zeebe-job-fail-dialog>
                    <zeebe-job-retries-dialog ${ref(this._jobRetriesDialogRef)} id="instance-job-retries-dialog" .context=${this.context}></zeebe-job-retries-dialog>
                    <zeebe-job-throw-error-dialog ${ref(this._jobThrowErrorDialogRef)} id="instance-job-throw-error-dialog" .context=${this.context}></zeebe-job-throw-error-dialog>
                </div>
                <div tab="process-instance-user-tasks">
                    <zeebe-table id="instance-user-tasks-table" .items=${this._item.userTasks}>
                        <vaadin-grid-column ${columnBodyRenderer(this._elementIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
                        <vaadin-grid-column header="Element Id" path="record.value.elementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Assignee" path="data.assignee" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Due Date" path="data.dueDate" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Follow Up Date" path="data.followUpDate" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Status" path="record.intent" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="data.created" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Actions" ${columnBodyRenderer(this._userTasksActionsRenderer, [])}></vaadin-grid-column>
                    </zeebe-table>
                    <zeebe-user-task-complete-dialog ${ref(this._userTaskCompleteDialogRef)} .context=${this.context}></zeebe-user-task-complete-dialog>
                    <zeebe-user-task-info-dialog ${ref(this._userTaskInfoDialogRef)} .context=${this.context}></zeebe-user-task-info-dialog>                    
                </div>
                <div tab="process-instance-messages">
                    <zeebe-table id="process-instance-messages-table" .items=${this._item.messageSubscriptions}>
                        <vaadin-grid-column ${columnBodyRenderer(this._elementIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
                        <vaadin-grid-column header="Element Id" path="record.value.elementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Message name" path="record.value.messageName" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Correlation Key" path="record.value.correlationKey" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="record.intent" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="data.time" resizable></vaadin-grid-column>                        
                        <vaadin-grid-column header="Actions" ${columnBodyRenderer(this._messageSubscriptionActionsRenderer, [])}></vaadin-grid-column>
                    </zeebe-table>
                    <zeebe-send-message-dialog ${ref(this._sendMessageDialogRef)} id="process-instance-send-message-dialog" .context=${this.context}></zeebe-send-message-dialog>
                </div>
                <div tab="process-instance-escalation">
                    <zeebe-table id="process-instance-escalations-table" .items=${this._item.escalations}>
                        <vaadin-grid-column ${columnBodyRenderer(this._throwElementIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
                        <vaadin-grid-column header="Throw Element Id" path="record.value.throwElementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column ${columnBodyRenderer(this._catchElementIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
                        <vaadin-grid-column header="Catch Element Id" path="record.value.catchElementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Code" path="record.value.escalationCode" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="data.time" resizable></vaadin-grid-column>
                    </zeebe-table>                    
                </div>
                <div tab="process-instance-timers">
                    <zeebe-table id="process-instance-timers-table" .items=${this._item.timers}>
                        <vaadin-grid-column ${columnBodyRenderer(this._targetElementIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
                        <vaadin-grid-column header="Element Id" path="record.value.targetElementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Due Date" path="data.dueDate" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Repetitions" path="record.value.repetitions" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="record.intent" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Time" path="data.time" resizable></vaadin-grid-column>
                    </zeebe-table>
                </div>
                <div tab="process-instance-called-instances">
                    <zeebe-table id="process-instance-called-instances-table" .items=${this._item.callProcessInstances}>
                        <vaadin-grid-column ${columnBodyRenderer(this._rootElementIdRenderer, [])} width="40px" flex-grow="0"></vaadin-grid-column>
                        <vaadin-grid-column header="Element Id" path="elementId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Instance Key" resizable ${columnBodyRenderer(this._instanceKeyRenderer, [])}></vaadin-grid-column>
                        <vaadin-grid-column header="Process Id" path="item.record.value.bpmnProcessId" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Process Definition Key" resizable ${columnBodyRenderer(this._processKeyRenderer, [])}></vaadin-grid-column>
                        <vaadin-grid-column header="State" path="item.data.state" resizable></vaadin-grid-column>
                    </zeebe-table>
                </div>
                <div tab="process-instance-errors">
                    <zeebe-table id="process-instance-errors-table" .items=${this._item.errors}>
                        <vaadin-grid-column header="Position" path="record.position"></vaadin-grid-column>
                        <vaadin-grid-column header="Error Event Position" 
                                            path="record.value.errorEventPosition"></vaadin-grid-column>
                        <vaadin-grid-column header="Exception Message"
                                            paht="record.value.exceptionMessage" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Stacktrace" path="record.value.stacktrace" resizable></vaadin-grid-column>
                        <vaadin-grid-column header="Created" path="data.created" resizable></vaadin-grid-column>
                    </zeebe-table>                    
                </div>
<!--                <div tab="process-instance-modify">5 This is the Dashboard tab content</div>-->

            </vaadin-tabsheet>        
        `;
    }

    _itemElementIdRenderer(item) {
        return diagramId(this._diagram, item.item.record.value.elementId);
    }

    _catchElementIdRenderer(item) {
        return diagramId(this._diagram, item.record.value.catchElementId);
    }

    _throwElementIdRenderer(item) {
        return diagramId(this._diagram, item.record.value.throwElementId);
    }

    _targetElementIdRenderer(item) {
        return diagramId(this._diagram, item.record.value.targetElementId);
    }
    _rootElementIdRenderer(item) {
        return diagramId(this._diagram, item.elementId);
    }

    _elementIdRenderer(item) {
        return diagramId(this._diagram, item.record.value.elementId);
    }

    _processKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "process", id: item.item.record.value.processDefinitionKey })}>${item.item.record.value.processDefinitionKey}</a>
        `;
    }

    _instanceKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "instance", id: item.item.record.value.processInstanceKey })}>${item.item.record.value.processInstanceKey}</a>
        `;
    }

    _messageSubscriptionActionsRenderer(item) {
        return html`
            <vaadin-icon icon="font-awesome-regular:envelope" style="color: var(--lumo-primary-text-color)"
                         title="Send message"
                         @click=${() => this._sendMessageDialogRef.value.open(item.record.value.messageName, item.record.value.correlationKey)}
            ></vaadin-icon>
        `;
    }

    _userTasksActionsRenderer(item) {
        return html`
            <vaadin-icon slot="prefix" icon="font-awesome-regular:circle-check"  title="Complete user task" style="color: var(--lumo-primary-text-color)"
                         ?hidden=${!item.active}
                         @click=${() => this._userTaskCompleteDialogRef.value.open(item)}
            ></vaadin-icon>
            <vaadin-icon slot="prefix" icon="font-awesome-regular:file-lines" title="Details" style="color: var(--lumo-primary-text-color)"
                         @click=${() => this._userTaskInfoDialogRef.value.open(item)}
            ></vaadin-icon>            
        `;
    }

    _jobsActionRenderer(item) {
        return html`
            <vaadin-icon icon="font-awesome-regular:circle-check" style="color: var(--lumo-primary-text-color)"
                         title="Complete job"
                         ?hidden=${!item.active || !this._item.active}
                         @click=${() => this._jobCompleteDialogRef.value.open(item)}
            ></vaadin-icon>
            <vaadin-icon icon="font-awesome-regular:circle-xmark" style="color: var(--lumo-primary-text-color)"
                         title="Fail job"
                         ?hidden=${!item.active || !this._item.active}
                         @click=${() => this._jobFailDialogRef.value.open(item)}
            ></vaadin-icon>
            <vaadin-icon icon="font-awesome-regular:circle-dot" style="color: var(--lumo-primary-text-color)"
                         title="Throw error for job"
                         ?hidden=${!item.active || !this._item.active}
                         @click=${() => this._jobThrowErrorDialogRef.value.open(item)}
            ></vaadin-icon>
            <vaadin-icon icon="font-awesome-solid:rotate" style="color: var(--lumo-primary-text-color)"
                         title="Update retries"
                         ?hidden=${!this._item.active}
                         @click=${() => this._jobRetriesDialogRef.value.open(item)}
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
                tmp.userTasks = tmp.userTasks.map((item) => ({
                    ...item,
                    active: item.record.intent === 'CREATED' || item.record.intent === 'FAILED'
                        || item.record.intent === 'TIMED_OUT' || item.record.intent == 'RETRIES_UPDATED',
                    searchTerms: `${item.record.value.name} ${item.record.value.correlationKey} ${item.record.value.messageId}`
                }));
                tmp.messageSubscriptions = tmp.messageSubscriptions.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.messageName}`
                }));
                tmp.escalations = tmp.escalations.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.escalationCode} ${item.record.value.throwElementId} ${item.record.value.catchElementId}`
                }));
                tmp.timers = tmp.timers.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.targetElementId} ${item.record.intent}`
                }));
                tmp.callProcessInstances = tmp.callProcessInstances.map((item) => ({
                    ...item,
                    searchTerms: `${item.item.record.value.bpmnProcessId} ${item.item.record.intent}`
                }));
                tmp.errors = tmp.errors.map((item) => ({
                    ...item,
                    searchTerms: `${item.item.record.value.exceptionMessage}`
                }));
                this._item = tmp;
            });
    }
}

customElements.define('zeebe-instance', ZeebeInstance);