import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import {ref, createRef} from 'lit/directives/ref.js';
import './components/zeebe-table.js';

export class ZeebeMessages extends LitElement {

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
            if (response.result.event === 'MESSAGE') {
                if (response.result.type === 'UPDATED') {
                    this._fetchData();
                }
            }
        });
    }

    _sendMessageDialogRef = createRef();

    disconnectedCallback() {
        this._observer.cancel();
        super.disconnectedCallback()
    }

    _fetchData() {
        this.jsonRpc.messages()
            .then(itemResponse => {
                this._items = itemResponse.result.map((item) => ({
                    ...item,
                    searchTerms: `${item.record.value.name} ${item.record.value.correlationKey} ${item.record.value.messageId}`
                }));
            });
    }

    render() {
        return html`
            <zeebe-table id="messages-table" .items=${this._items}>
                <vaadin-button slot="toolbar" theme="primary" style="align-self: end" @click=${() => this._sendMessageDialogRef.value.open(null, true, true)}>
                    <vaadin-icon slot="prefix" icon="font-awesome-solid:envelope"></vaadin-icon>
                    Send message
                </vaadin-button>
                
                <vaadin-grid-column header="Message name" path="data.name" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Correlation Key" path="data.correlationKey" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Message Id" path="data.messageId" resizable></vaadin-grid-column>
                <vaadin-grid-column header="State" path="record.intent"></vaadin-grid-column>
                <vaadin-grid-column header="Time" path="data.time"></vaadin-grid-column>
            </zeebe-table>
            <zeebe-send-message-dialog ${ref(this._sendMessageDialogRef)} id="processes-send-message-dialog" .context=${this.context}></zeebe-send-message-dialog>
        `;
    }
}

customElements.define('zeebe-messages', ZeebeMessages);