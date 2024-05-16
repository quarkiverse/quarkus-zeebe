import { JsonRpc } from 'jsonrpc';
import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';
import { notifier } from 'notifier';

export class ZeebeIncidentResolveDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _item: { state: true },
        _retries: { state: true },
        _isJobKey: { state: true },
    }

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._opened = false;
        this._item = null;
        this._retries = 0;
        this._isJobKey = false;
    }

    open(item) {
        this._item = item;
        this._isJobKey = this._item.item.record.value.jobKey > 0;
        this._retries = 1;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="incident-resolve-dialog" header-title="Resolve incident" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._retries, this._isJobKey, this._item])}
                           ${dialogFooterRenderer(this._footer, [this._retries])}
            >
            </vaadin-dialog>            
        `;
    }

    _render() {
        return html`
            <vaadin-vertical-layout style="align-items: stretch; width:100%; min-width: 400px; min-height: 200px; max-height: 600px;">
                <h3>Make sure you have fixed the error</h3>
                <vaadin-integer-field label="Job retries"
                                      min="1"
                                      step-buttons-visible
                                      helper-text="Minimum 1 retry"
                                   value="${this._retries}"
                                   ?hidden=${!this._isJobKey}
                                   @value-changed=${(e) => {this._retries = e.detail.value;}}>
                </vaadin-integer-field>
                <vaadin-text-area
                        readonly
                        style="width:100%; min-width: 400px; min-height: 100px; max-height: 300px;"
                        label="Error message"
                        value="${this._item.item.record.value.errorMessage}"
                ></vaadin-text-area>                
            </vaadin-vertical-layout>   
        `;
    }

    _footer = () => html`
        <vaadin-button theme="tertiary" @click="${this._close}">Close</vaadin-button>
        <vaadin-button theme="primary" @click=${this._action} ?disabled=${this._retries < 1}>Resolve</vaadin-button>
    `;

    _close() {
        this._opened = false
    }

    _action() {
        this.jsonRpc.resolveIncident({key: this._item.item.record.key, jobKey: this._item.item.record.value.jobKey, retries: Number(this._retries)})
            .then(response => {
                console.log(response);
                this._close();
            })
            .catch(e => {
                console.log(e);
                notifier.showErrorMessage('Resolve incident error: ' + e.error.code + ' detail: ' + e.error.message, null);
            });
    }

}

customElements.define('zeebe-incident-resolve-dialog', ZeebeIncidentResolveDialog);