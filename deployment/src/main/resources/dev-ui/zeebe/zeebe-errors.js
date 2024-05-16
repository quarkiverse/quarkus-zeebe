import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import './components/zeebe-table.js';

export class ZeebeErrors extends LitElement {

    static properties = {
        _items: {state: true},
        navigation: {},
    };

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._fetchData();

        this._observer = this.jsonRpc.notifications().onNext(response => {
            if (response.result.event === 'ERROR') {
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
        this.jsonRpc.errors()
            .then(itemResponse => {
                this._items = itemResponse.result.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.exceptionMessage} ${item.record.value.processInstanceKey}`
                }));
            });
    }

    render() {
        return html`
            <zeebe-table id="errors-table" .items=${this._items}>
                <vaadin-grid-column header="Position" path="record.position"></vaadin-grid-column>
                <vaadin-grid-column header="Error Event Position" path="record.value.errorEventPosition"
                                    ></vaadin-grid-column>
                <vaadin-grid-column header="Instance Key"
                                    ${columnBodyRenderer(this._instanceKeyRenderer, [])}></vaadin-grid-column>
                <vaadin-grid-column header="Exception Message"
                                    paht="record.value.exceptionMessage" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Stacktrace" path="record.value.stacktrace" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Created" path="data.created" resizable></vaadin-grid-column>
            </zeebe-table>
        `;
    }

    _instanceKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({
                nav: "instance",
                id: item.record.value.processInstanceKey
            })}>${item.record.value.processInstanceKey}</a>
        `;
    }
}

customElements.define('zeebe-errors', ZeebeErrors);