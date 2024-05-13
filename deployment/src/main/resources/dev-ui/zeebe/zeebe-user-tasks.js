import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import './components/zeebe-table.js';

export class ZeebeUserTasks extends LitElement {

    static properties = {
        _items: {state: true},
        navigation: {},
        context: {},
    };

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
                    searchTerms: `${item.record.value.name} ${item.record.value.correlationKey} ${item.record.value.messageId}`
                }));
            });
    }

    render() {
        return html`
            <zeebe-table id="user-tasks-table" .items=${this._items}>
                <vaadin-grid-column header="Process Instance Key" ${columnBodyRenderer(this._instanceKeyRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="Element" path="record.value.elementId" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Assignee" path="data.assignee" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Due Date" path="record.dueDate"></vaadin-grid-column>
                <vaadin-grid-column header="Follow Up" path="data.followUpDate"></vaadin-grid-column>
                <vaadin-grid-column header="Status" path="record.intent"></vaadin-grid-column>
                <vaadin-grid-column header="Created" path="data.time"></vaadin-grid-column>
                <vaadin-grid-column header="Actions"></vaadin-grid-column>
            </zeebe-table>
        `;
    }

    _instanceKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "instance", id: item.record.value.processInstanceKey })}>${item.record.value.processInstanceKey}</a>
        `;
    }
}

customElements.define('zeebe-user-tasks', ZeebeUserTasks);