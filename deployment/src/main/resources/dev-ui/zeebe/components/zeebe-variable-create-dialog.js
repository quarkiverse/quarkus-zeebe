import { JsonRpc } from 'jsonrpc';
import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';
import { notifier } from 'notifier';

export class ZeebeVariableCreateDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _item: { state: true },
        _name: { state: true },
        _scope: { state: true },
        _scopes: { state: true },
        _value: { state: true }
    }

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._opened = false;
        this._scopes = [];
        this._scope = null;
        this._name = null;
        this._value = null;
    }

    open(item, scopes) {
        this._item = item;
        this._opened = true;
        this._scope = null;
        this._name = null;
        this._value = null;
        this._scopes = scopes;
    }

    render() {
        return html`
            <vaadin-dialog id="variable-create-dialog" header-title="Create variable" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._scope, this._scopes, this._name, this._value])}
                           ${dialogFooterRenderer(this._footer, [this._scope, this._name])}
            >
            </vaadin-dialog>            
        `;
    }

    _render() {
        return html`
            <vaadin-vertical-layout style="align-items: stretch; width:100%; min-width: 400px; min-height: 200px; max-height: 600px;">
                <vaadin-text-field label="Name"
                                   value="${this._name}"
                                   @value-changed=${(e) => {this._name = e.detail.value;}}>
                </vaadin-text-field>
                <vaadin-combo-box label="Scope"
                                  item-label-path="name"
                                  item-value-path="value"
                                  .value=${this._scope}
                                  .items=${this._scopes}
                                  @value-changed=${(e) => {this._scope = e.detail.value;}}>
                </vaadin-combo-box>
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
        <vaadin-button theme="primary" @click=${this._action}  ?disabled=${!this._scope || !this._name || this._name.length === 0}>Create</vaadin-button>
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

        this.jsonRpc.setVariables({key: this._scope, local: true, variables:obj})
            .then(response => {
                console.log(response);
                this._close();
            })
            .catch(e => {
                console.log(e);
                notifier.showErrorMessage('Create variable error: ' + e.error.code + ' detail: ' + e.error.message, null);
            });
    }

}

customElements.define('zeebe-variable-create-dialog', ZeebeVariableCreateDialog);