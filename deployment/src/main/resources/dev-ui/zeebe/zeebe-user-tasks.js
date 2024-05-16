import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import {ref, createRef} from 'lit/directives/ref.js';
import './components/zeebe-table.js';
import './components/zeebe-user-task-info-dialog.js';
import './components/zeebe-user-task-complete-dialog.js';

export class ZeebeUserTasks extends LitElement {

    static properties = {
        _items: {state: true},
        navigation: {},
        context: {},
    };

    _userTaskCompleteDialogRef = createRef();
    _userTaskInfoDialogRef = createRef();

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._fetchData();
        this._observer = this.jsonRpc.notifications().onNext(response => {
            if (response.result.event === 'USER_TASK') {
                if (response.result.type === 'UPDATED') {
                    this._fetchData();
                }
            }
        });
    }

    disconnectedCallback() {
        this._observer.cancel();
        super.disconnectedCallback()
    }

    _fetchData() {
        this.jsonRpc.userTasks()
            .then(itemResponse => {
                this._items = itemResponse.result.map((item) => ({
                    ...item,
                    active: item.record.intent === 'CREATED' || item.record.intent === 'FAILED'
                                || item.record.intent === 'TIMED_OUT' || item.record.intent == 'RETRIES_UPDATED',
                    searchTerms: `${item.record.value.name} ${item.record.value.correlationKey} ${item.record.value.messageId}`
                }));

                console.log(this._items);
            });

    }

    render() {
        return html`
            <zeebe-table id="user-tasks-table" .items=${this._items}>
                <vaadin-grid-column header="Process Instance Key" ${columnBodyRenderer(this._instanceKeyRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="Element Id" path="record.value.elementId" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Assignee" path="data.assignee" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Due Date" path="data.dueDate" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Follow Up Date" path="data.followUpDate" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Status" path="record.intent" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Time" path="data.created" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Actions" ${columnBodyRenderer(this._actionsRenderer, [])}></vaadin-grid-column>
            </zeebe-table>
            <zeebe-user-task-complete-dialog ${ref(this._userTaskCompleteDialogRef)} .context=${this.context}></zeebe-user-task-complete-dialog>
            <zeebe-user-task-info-dialog ${ref(this._userTaskInfoDialogRef)} .context=${this.context}></zeebe-user-task-info-dialog>
        `;
    }

    _instanceKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "instance", id: item.record.value.processInstanceKey })}>${item.record.value.processInstanceKey}</a>
        `;
    }

    _actionsRenderer(item) {
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
}

customElements.define('zeebe-user-tasks', ZeebeUserTasks);