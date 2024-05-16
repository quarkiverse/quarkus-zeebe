import { JsonRpc } from 'jsonrpc';
import { notifier } from 'notifier';
import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';

export class ZeebeInstanceCancelDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _id: { state: true },
    }

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
        this.jsonRpc = new JsonRpc(this.context.extension);
    }

    open(id) {
        this._id = id;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="instance-cancel-dialog" header-title="Cancel process instance?" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._id])}
                           ${dialogFooterRenderer(this._footer, [])}
            >
            </vaadin-dialog>            
        `;
    }

    _render() {
        return html`Are you sure you want to cancel ${this._id} process instance?`;
    }

    _footer = () => html`
        <vaadin-button theme="tertiary" @click="${this._close}">No, cancel</vaadin-button>
        <vaadin-button theme="primary error" @click=${this._action}>Yes, I'm sure</vaadin-button>        
    `;

    _close() {
        this._opened = false
    }

    _action() {
        this.jsonRpc.cancelProcessInstance({processInstanceKey: this._id})
            .then(response => {
                console.log(response);
                this._close();
            })
            .catch(e => {
                console.log(e);
                notifier.showErrorMessage('Cancel process instance error: ' + e.error.code + ' message: ' + e.error.message, null)
            });
    }

}

customElements.define('zeebe-instance-cancel-dialog', ZeebeInstanceCancelDialog);