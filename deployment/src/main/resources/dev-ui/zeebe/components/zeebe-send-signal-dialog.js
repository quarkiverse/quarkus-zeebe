import { JsonRpc } from 'jsonrpc';
import { notifier } from 'notifier';
import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';


export class ZeebeSendSignalDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _name: { state: true },
        _editName: { state: true },
        _variables: { state: true },
    }

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
        this.jsonRpc = new JsonRpc(this.context.extension);
    }

    open(name, editName = false) {
        this._name = name;
        this._editName = editName;
        this._variables = null;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="send-signal-dialog" header-title="Send signal" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._name, this._variables, this._editName])}
                           ${dialogFooterRenderer(this._footer, [])}
            >
            </vaadin-dialog>
        `;
    }

    _footer = () => html`
        <vaadin-button @click="${this._close}">Cancel</vaadin-button>
        <vaadin-button theme="primary" @click=${this._action}>Send</vaadin-button>        
    `;

    _close() {
        this._opened = false
    }

    _action() {
        let variables = {};
        if (this._variables) {
            try {
                variables = JSON.parse(this._variables);
            } catch (e) {
                notifier.showErrorMessage(e.message, null);
                return;
            }
        }

        this.jsonRpc.sendSignal({name: this._name, variables: variables})
            .then(response => {
                console.log(response);
                this._close();
            })
            .catch(e => {
                console.log(e);
                notifier.showErrorMessage('Send signal error: ' + e.error.code + ' detail: ' + e.error.message, null);
            });
    }

    _render() {
        return html`
            <vaadin-vertical-layout style="align-items: stretch; width:100%; min-width: 400px; min-height: 200px; max-height: 600px;">
                <vaadin-text-field label="Signal name" 
                                   value="${this._name}" 
                                   disabled="${!this._editName}" 
                                   @value-changed=${(e) => {this._name = e.detail.value;}}>
                </vaadin-text-field>
                <vaadin-text-area
                        style="width:100%; min-width: 400px; min-height: 200px; max-height: 300px;"
                        label="Variables"
                        helper-text="Variables in JSON format"
                        placeholder='{"name":"value"}'
                        value="${this._variables}"
                        @value-changed=${(e) => {this._variables = e.detail.value;}}                        
                ></vaadin-text-area>
            </vaadin-vertical-layout>            
        `;
    }

}

customElements.define('zeebe-send-signal-dialog', ZeebeSendSignalDialog);