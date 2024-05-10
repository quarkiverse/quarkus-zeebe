import { JsonRpc } from 'jsonrpc';
import { LitElement, html } from 'lit';
import './zeebe-dialog.js';
import { notifier } from 'notifier';

export class ZeebeSendMessageDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        _item: {},
    }

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
        this.jsonRpc = new JsonRpc(this.context.extension);
    }

    open(item) {
        this._item = item;
        this._item.duration = "PT0S"
        this._item.parameters = null;
        this._opened = true;
    }

    render() {
        return html`
            <zeebe-dialog id="send-message-dialog" title="Send message" titleAction="Send" .opened=${this._opened}
                          .renderDialog=${() => this._render()}
                          .actionDialog=${() => this._action()}
                          .closeDialog=${() => this._close()}
                          .renderDialogParams=${() => [this._item.duration, this._item.parameters]}
            >
            </zeebe-dialog>        
        `;
    }

    _close() {
        this._opened = false
    }

    _action() {
        console.log(this._item);
        let parameters = {};

        if (this._item.parameters) {
            try {
                parameters = JSON.parse(this._item.parameters);
            } catch (e) {
                notifier.showErrorMessage(e.message, null);
                return;
            }
        }

        this.jsonRpc.sendMessage({name: this._item.name, correlationKey: this._item.correlationKey, duration: this._item.duration, variables: parameters})
            .then(response => {
                console.log(response);
                this._close();
            })
            .catch(e => {
                console.log(e);
                notifier.showErrorMessage('Send message error: ' + e.error.code + ' detail: ' + e.error.message, null);
            });
    }

    _render() {
        return html`
            <vaadin-vertical-layout style="align-items: stretch; width:100%; min-width: 400px; min-height: 200px; max-height: 600px;">
                <vaadin-text-field label="Message name" 
                                   value="${this._item.name}" 
                                   disabled="${!this._item.editName}" 
                                   @value-changed=${(e) => {this._item.name = e.detail.value;}}>
                </vaadin-text-field>
                <vaadin-text-field label="Correlation key" 
                                   value="${this._item.key}" 
                                   disabled="${this._item.key != null}" 
                                   @value-changed=${(e) => {this._item.key = e.detail.value;}}>
                </vaadin-text-field>
                <vaadin-text-field label="Time to live (duration)"
                                   value="${this._item.duration}" 
                                   @value-changed=${(e) => {this._item.duration = e.detail.value;}}>
                </vaadin-text-field>
                <vaadin-text-area
                        style="width:100%; min-width: 400px; min-height: 100px; max-height: 300px;"
                        label="Parameters"
                        helper-text="Parameters in JSON format"
                        placeholder='{"parameter":"value"}'
                        value="${this._item.parameters}"
                        @value-changed=${(e) => {this._item.parameters = e.detail.value;}}                        
                ></vaadin-text-area>
            </vaadin-vertical-layout>            
        `;
    }

}

customElements.define('zeebe-send-message-dialog', ZeebeSendMessageDialog);