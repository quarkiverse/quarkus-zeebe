import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import { notifier } from 'notifier';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';
import {ref, createRef} from 'lit/directives/ref.js';
import {when} from 'lit/directives/when.js';
import '@vaadin/upload';

export class ZeebeProcessDeployDialog extends LitElement {


    static properties = {
        _opened: {state: true},
        context: {},
        _xml: {state: true},
        _name: {state: true},
        _b: { state: true},
    }

    _uploadRef = createRef();

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._name = '';
        this._xml = null;
        this._b = true;
    }

    open() {
        this._opened = true;
        this._name = '';
        this._xml = null;
        this._b = true;
        if (this._uploadRef.value) {
            this._uploadRef.value.value = null;
        }
    }

    render() {
        return html`
            <vaadin-dialog id="process-deploy-dialog" header-title="Deploy new process" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._name, this._b])}
                           ${dialogFooterRenderer(() => this._footer(), [this._b])}
            >
            </vaadin-dialog>
        `;
    }

    _footer() {
        return html`
            <vaadin-button theme="tertiary" @click="${this._close}">Cancel</vaadin-button>
            <vaadin-button theme="primary" ?disabled=${this._b} @click=${this._action}>Deploy</vaadin-button>
        `;
    }

    _close() {
        this._opened = false
    }

    _render() {
        return html`
            <vaadin-horizontal-layout theme="spacing padding"  style="align-items: stretch; width:100%; min-width: 400px;">
                <vaadin-button @click="${() => this._uploadRef.value.click()}">Upload File...</vaadin-button>
                <div style="display: none;"><input hidden ${ref(this._uploadRef)} name="file" accept=".bpmn" type="file" @change=${(e) => this._uploadFile(e)}/></div>
                <p>${this._name}</p>
            </vaadin-horizontal-layout>
        `;
    }

    _uploadFile(e) {
        let files = e.target.files;
        this._b = true;
        let reader = new FileReader();
        reader.onload = (e) => {
            this._xml = e.target.result;
            this._b = false;
        }
        reader.readAsText(files[0]);
        this._name = files[0].name;
    }

    _action() {
        this.jsonRpc.deployProcess({name: this._name, xml: this._xml})
            .then(response => {
                console.log(response);
                this._close();
            })
            .catch(e => {
                console.log(e);
                notifier.showErrorMessage('Deploy process error: ' + e.error.code + ' detail: ' + e.error.message, null);
            });
    }
}

customElements.define('zeebe-process-deploy-dialog', ZeebeProcessDeployDialog);