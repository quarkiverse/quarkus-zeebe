import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import {ref, createRef} from 'lit/directives/ref.js';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import './components/zeebe-table.js';

export class ZeebeSignals extends LitElement {

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
            if (response.result.event === 'SIGNAL') {
                if (response.result.type === 'UPDATED') {
                    this._fetchData();
                }
            }
        });
    }

    _sendSignalDialogRef = createRef();

    disconnectedCallback() {
        this._observer.cancel();
        super.disconnectedCallback()
    }

    _fetchData() {
        this.jsonRpc.signals()
            .then(itemResponse => {
                this._items = itemResponse.result.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.signalName}`
                }));
            });
    }

    render() {
        return html`
            <zeebe-table id="messages-table" .items=${this._items}>
                <vaadin-button slot="toolbar" theme="primary" style="align-self: end" @click=${() => this._sendSignalDialogRef.value.open(null, true)}>
                    <vaadin-icon slot="prefix" icon="font-awesome-solid:envelope"></vaadin-icon>
                    Broadcast signal
                </vaadin-button>
                
                <vaadin-grid-column header="Signal name" path="record.value.signalName" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Variables" ${columnBodyRenderer(this._variablesRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="Status" path="record.intent"></vaadin-grid-column>
                <vaadin-grid-column header="Time" path="data.time"></vaadin-grid-column>
            </zeebe-table>
            <zeebe-send-signal-dialog ${ref(this._sendSignalDialogRef)} id="signals-send-signal-dialog" .context=${this.context}></zeebe-send-signal-dialog>
        `;
    }

    _variablesRenderer(item) {
        return html`${JSON.stringify(item.record.value.variables)}`
    }
}

customElements.define('zeebe-signals', ZeebeSignals);