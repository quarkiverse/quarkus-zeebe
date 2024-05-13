import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import './components/zeebe-table.js';

export class ZeebeJobs extends LitElement {

    static properties = {
        _items: {state: true},
        navigation: {},
    };

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._fetchData();

        this._observer = this.jsonRpc.notifications().onNext(response => {
            if (response.result.event === 'JOB') {
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
        this.jsonRpc.jobs()
            .then(itemResponse => {
                this._items = itemResponse.result.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.type} ${item.id} ${item.record.value.processInstanceKey} `
                }));
            });
    }

    render() {
        return html`
            <zeebe-table id="jobs-table" .items=${this._items}>
                <vaadin-grid-column header="Job Key" path="record.key" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Job Type" path="record.value.type" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Process Instance Key" ${columnBodyRenderer(this._instanceKeyRenderer, [])}></vaadin-grid-column>
                <vaadin-grid-column header="Retries" path="record.value.retries"></vaadin-grid-column>
                <vaadin-grid-column header="State" path="record.intent" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Timestamp" path="data.time" resizable></vaadin-grid-column>                
            </zeebe-table>
        `;
    }

    _instanceKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "instance", id: item.record.value.processInstanceKey })}>${item.record.value.processInstanceKey}</a>
        `;
    }

}

customElements.define('zeebe-jobs', ZeebeJobs);