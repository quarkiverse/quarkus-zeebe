import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';

export class ZeebeVariableCreateDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _item: { state: true },
        _name: { state: true },
    }

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
    }

    open(item) {
        this._item = item;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="variable-create-dialog" header-title="Create variable" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._item])}
                           ${dialogFooterRenderer(this._footer, [])}
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
                                  value="${this._scope}" 
                                  .items=['a','b']
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
        <vaadin-button theme="primary" @click=${this._action}>Create</vaadin-button>
    `;

    _close() {
        this._opened = false
    }

    _action() {

    }

}

customElements.define('zeebe-variable-create-dialog', ZeebeVariableCreateDialog);