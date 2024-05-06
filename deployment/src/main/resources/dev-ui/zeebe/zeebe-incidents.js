import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import './components/zeebe-table.js';

export class ZeebeIncidents extends LitElement {

    static properties = {
        _items: {state: true},
        navigation: {},
    };

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._fetchData();

        this._observer = this.jsonRpc.notifications().onNext(eventResponse => {
            this._fetchData();
        });
    }

    disconnectedCallback() {
        this._observer.cancel();
        super.disconnectedCallback()
    }

    _fetchData() {
        this.jsonRpc.incidents()
            .then(itemResponse => {
                this._items = itemResponse.result.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.bpmnProcessId} ${item.record.value.processInstanceKey} ${item.id} ${item.record.value.processDefinitionKey} `
                }));
            });
    }

    render() {
        return html`
            <zeebe-table .items=${this._items}>
                <vaadin-grid-column header="Incident Key" path="record.key" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Instance Key" ${columnBodyRenderer(this._instanceKeyRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="Process Id" path="record.value.bpmnProcessId" ></vaadin-grid-column>
                <vaadin-grid-column header="Definition Key" ${columnBodyRenderer(this._processKeyRenderer, [])}></vaadin-grid-column>
                <vaadin-grid-column header="Type" path="record.value.errorType" resizable></vaadin-grid-column>
                <vaadin-grid-column header="State" path="record.intent" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Created" path="data.created" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Resolved" path="data.resolved" resizable></vaadin-grid-column>
            </zeebe-table>
        `;
    }

    _instanceKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "instance", id: item.record.value.processInstanceKey })}>${item.record.value.processInstanceKey}</a>
        `;
    }

    _processKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "process", id: item.record.value.processDefinitionKey })}>${item.record.value.processDefinitionKey}</a>
        `;
    }

}

customElements.define('zeebe-incidents', ZeebeIncidents);