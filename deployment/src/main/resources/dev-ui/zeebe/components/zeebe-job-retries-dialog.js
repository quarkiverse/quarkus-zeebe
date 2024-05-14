import { JsonRpc } from 'jsonrpc';
import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';
import { notifier } from 'notifier';

export class ZeebeJobRetriesDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _item: { state: true },
        _retries: { state: true },
    }

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._opened = false;
        this._item = null;
        this._retries = 1;
    }

    open(item) {
        this._item = item;
        this._retries = 1;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="job-retries-dialog" header-title="Update job retries" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._retries, this._item])}
                           ${dialogFooterRenderer(this._footer, [this._retries])}
            >
            </vaadin-dialog>            
        `;
    }

    _render() {
        return html`
            <vaadin-vertical-layout style="align-items: stretch; width:100%; min-width: 400px; min-height: 200px; max-height: 600px;">
                <vaadin-text-field label="Element Id" value="${this._item.record.value.elementId}" readonly ></vaadin-text-field>
                <vaadin-text-field label="Job Type" value="${this._item.record.value.type}" readonly ></vaadin-text-field>
                <vaadin-integer-field label="Job retries"
                                      min="1"
                                      step-buttons-visible
                                      helper-text="Minimum 1 retry"
                                      value="${this._retries}"
                                      @value-changed=${(e) => {this._retries = e.detail.value;}}>
                </vaadin-integer-field>                
            </vaadin-vertical-layout>   
        `;
    }

    _footer = () => html`
        <vaadin-button theme="tertiary" @click="${this._close}">Close</vaadin-button>
        <vaadin-button theme="primary" @click=${this._action} ?disabled=${this._retries < 1}>Update retries</vaadin-button>
    `;

    _close() {
        this._opened = false
    }

    _action() {
        this.jsonRpc.jobRetries({key: this._item.record.key, retries: Number(this._retries)})
            .then(response => {
                console.log(response);
                this._close();
            })
            .catch(e => {
                console.log(e);
                notifier.showErrorMessage('Job retries complete error: ' + e.error.code + ' detail: ' + e.error.message, null);
            });
    }

}

customElements.define('zeebe-job-retries-dialog', ZeebeJobRetriesDialog);