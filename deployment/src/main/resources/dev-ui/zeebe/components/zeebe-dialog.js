import { LitElement, html, css } from 'lit';
import { dialogRenderer, dialogHeaderRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';

export class ZeebeDialog extends LitElement {

    static properties = {
        title: {},
        titleAction: {},
        renderDialog: {},
        actionDialog: {},
        closeDialog: {},
        _opened: { state: true },
        opened: {},
    };

    set opened(val) {
        this._opened = val;
    }

    constructor() {
        super();
        this.title = "Title";
        this.titleAction = "Action";
        this.actionDialog = this._close;
        this.renderDialog = this._renderDialog;
    }

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
    }

    render() {
        return html`
            <vaadin-dialog header-title="${this.title}" .opened=${this._opened}
                   @opened-changed=${(e) => {this.opened = e.detail.value;}}
                   ${dialogHeaderRenderer(this._dialogHeaderRenderer, [] )}
                   ${dialogRenderer(this.renderDialog, [])}
                   ${dialogFooterRenderer(this._renderFooter, [])}
            >
            </vaadin-dialog>        
        `;
    }

    _renderDialog = () => html`
        <p>Default dialog text</p>
    `;

    _close() {
        this._opened = false;
        this.closeDialog();
    }

    _renderFooter = () => html`
        <vaadin-button @click="${this._close}">Cancel</vaadin-button>
        <vaadin-button theme="primary" @click=${this._action}>${this.titleAction}</vaadin-button>        
    `;

    _action() {
        this.actionDialog();
    }

    _dialogHeaderRenderer = () => html`<vaadin-icon @click=${this._close} icon="font-awesome-solid:xmark"></vaadin-icon>`;
}

customElements.define('zeebe-dialog', ZeebeDialog);