import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';

export class ZeebeVariableEditDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _item: { state: true },
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
            <vaadin-dialog id="variable-edit-dialog" header-title="Update variable" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._item])}
                           ${dialogFooterRenderer(this._footer, [])}
            >
            </vaadin-dialog>            
        `;
    }

    _render() {
        return html`

        `;
    }

    _footer = () => html`
        <vaadin-button theme="tertiary" @click="${this._close}">Close</vaadin-button>
        <vaadin-button theme="primary" @click=${this._action}>Update</vaadin-button>
    `;

    _close() {
        this._opened = false
    }

    _action() {

    }

}

customElements.define('zeebe-variable-edit-dialog', ZeebeVariableEditDialog);