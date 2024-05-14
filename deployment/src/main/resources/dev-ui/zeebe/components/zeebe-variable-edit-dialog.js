import { JsonRpc } from 'jsonrpc';
import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';
import { notifier } from 'notifier';

export class ZeebeVariableEditDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _name: { state: true },
        _scope: { state: true },
        _value: { state: true },
    }

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._scope = null;
        this._name = null;
        this._value = null;
    }

    open(name, scope, value) {
        this._name = name;
        this._scope = scope;
        this._value = value;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="variable-edit-dialog" header-title="Update variable" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._name, this._scope, this._value])}
                           ${dialogFooterRenderer(this._footer, [this._scope, this._name])}
            >
            </vaadin-dialog>            
        `;
    }

    _render() {
        return html`
            <vaadin-vertical-layout style="align-items: stretch; width:100%; min-width: 400px; min-height: 200px; max-height: 600px;">
                <vaadin-text-field label="Name" value="${this._name}" readonly></vaadin-text-field>
                <vaadin-text-field label="Scope" value="${this._scope}" readonly></vaadin-text-field>
                <vaadin-text-area
                        style="width:100%; min-width: 400px; min-height: 100px; max-height: 300px;"
                        label="Value"
                        helper-text="Value in JSON format"
                        value="${this._value}"
                        @value-changed=${(e) => {this._value = e.detail.value;}}
                ></vaadin-text-area>
            </vaadin-vertical-layout>
        `;
    }

    _footer = () => html`
        <vaadin-button theme="tertiary" @click="${this._close}">Close</vaadin-button>
        <vaadin-button theme="primary" @click=${this._action} 
                       ?disabled=${!this._scope || !this._name || this._name.length === 0}>
            Update
        </vaadin-button>
    `;

    _close() {
        this._opened = false
    }

    _action() {
        let tmp = null
        if (this._value) {
            try {
                tmp = JSON.parse(this._value)
            } catch (e) {
                notifier.showErrorMessage(e.message, null);
                return;
            }
        }
        let obj = {};
        obj[this._name] = tmp;

        this.jsonRpc.setVariables({key: this._scope, local: false, variables:obj})
            .then(response => {
                console.log(response);
                this._close();
            })
            .catch(e => {
                console.log(e);
                notifier.showErrorMessage('Update variable error: ' + e.error.code + ' detail: ' + e.error.message, null);
            });
    }

}

customElements.define('zeebe-variable-edit-dialog', ZeebeVariableEditDialog);