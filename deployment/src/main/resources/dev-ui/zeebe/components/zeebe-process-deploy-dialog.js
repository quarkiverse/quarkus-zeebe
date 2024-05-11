import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';
import {ref, createRef} from 'lit/directives/ref.js';
import '@vaadin/upload';

export class ZeebeProcessDeployDialog extends LitElement {

    static properties = {
        _opened: { state: true },
    }

    _uploadRef = createRef();

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
    }

    open() {
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="process-deploy-dialog" header-title="Deploy new process" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._key, this._variables])}
                           ${dialogFooterRenderer(this._footer, [])}
            >
            </vaadin-dialog>
        `;
    }

    _footer = () => html`
        <vaadin-button @click="${this._close}">Cancel</vaadin-button>
    `;

    _close() {
        this._opened = false
        this._uploadRef.value.files = [];
    }

    _render() {
        return html`
            <p>Accepted file formats: BPMN (.bpmn)</p>
            <vaadin-upload ${ref(this._uploadRef)}
                   id="deploy-process" 
                   nodrop 
                   style="width: 400px; max-width: 100%; align-items: stretch;"
                   accept=".bpmn"
                   method="POST"
                   target="/q/zeebe/ui/cmd/process-deploy"
                   @upload-success=${() => this._close()}
            ></vaadin-upload>
        `;
    }

}

customElements.define('zeebe-process-deploy-dialog', ZeebeProcessDeployDialog);