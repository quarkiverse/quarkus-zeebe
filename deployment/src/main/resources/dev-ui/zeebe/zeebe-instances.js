import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import './components/zeebe-table.js';

export class ZeebeInstances extends LitElement {

    static properties = {
        _items: {state: true},
        navigation: {},
    };

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._fetchData();

        this._observer = this.jsonRpc.notifications().onNext(response => {
            this._fetchData();
            if (response.result.event === 'PROCESS_INSTANCE') {
                this._fetchData();
            }
        });
    }

    disconnectedCallback() {
        this._observer.cancel();
        super.disconnectedCallback()
    }

    _fetchData() {
        this.jsonRpc.instances()
            .then(itemResponse => {
                this._items = itemResponse.result.map((item) => ({
                    ...item,
                    searchTerms: `${item.id} ${item.record.value.bpmnProcessId} ${item.record.value.processDefinitionKey}`
                }));
            });
    }

    render() {
        return html`
            <zeebe-table id="instances-table" .items=${this._items}>
                <vaadin-grid-column header="Process Instance Key" ${columnBodyRenderer(this._instanceKeyRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="Process Id" path="record.value.bpmnProcessId" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Process key" path="record.value.processDefinitionKey"></vaadin-grid-column>
                <vaadin-grid-column header="State" path="data.state"></vaadin-grid-column>
                <vaadin-grid-column header="Start time" path="data.start" resizable></vaadin-grid-column>
                <vaadin-grid-column header="End time" path="data.end" resizable></vaadin-grid-column>                
            </zeebe-table>
        `;
    }

    _instanceKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "instance", id: item.id })}>${item.id}</a>
        `;
    }

}

customElements.define('zeebe-instances', ZeebeInstances);