import { JsonRpc } from 'jsonrpc';
import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';
import { notifier } from 'notifier';

export class ZeebeJobCompleteDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _item: { state: true },
        _variables: { state: true },
    }

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._opened = false;
        this._item = null;
        this._variables = null;
    }

    open(item) {
        this._item = item;
        this._variables = null;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="job-complete-dialog" header-title="Complete job" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._variables, this._item])}
                           ${dialogFooterRenderer(this._footer, [])}
            >
            </vaadin-dialog>            
        `;
    }

    _render() {
        return html`
            <vaadin-vertical-layout style="align-items: stretch; width:100%; min-width: 400px; min-height: 200px; max-height: 600px;">
                <vaadin-text-field label="Element Id" value="${this._item.record.value.elementId}" readonly ></vaadin-text-field>
                <vaadin-text-field label="Job Type" value="${this._item.record.value.type}" readonly ></vaadin-text-field>
                <vaadin-text-area
                        style="width:100%; min-width: 400px; min-height: 100px; max-height: 300px;"
                        label="Input variables"
                        helper-text="Variables in JSON format"
                        value="${JSON.stringify(this._item.record.value.variables)}"
                        readonly
                ></vaadin-text-area>
                <vaadin-text-area
                        style="width:100%; min-width: 400px; min-height: 100px; max-height: 300px;"
                        label="Output variables"
                        helper-text="Variables in JSON format"
                        placeholder='{"name":"value"}'
                        value="${this._variables}"
                        @value-changed=${(e) => {this._variables = e.detail.value;}}
                ></vaadin-text-area>             
            </vaadin-vertical-layout>   
        `;
    }

    _footer = () => html`
        <vaadin-button theme="tertiary" @click="${this._close}">Close</vaadin-button>
        <vaadin-button theme="primary" @click=${this._action}>Complete job</vaadin-button>
    `;

    _close() {
        this._opened = false
    }

    _action() {

        let variables = {};

        if (this._variables) {
            try {
                variables = JSON.parse(this._variables);
            } catch (e) {
                notifier.showErrorMessage(e.message, null);
                return;
            }
        }

        this.jsonRpc.jobComplete({key: this._item.record.key, variables: variables})
            .then(response => {
                console.log(response);
                this._close();
            })
            .catch(e => {
                console.log(e);
                notifier.showErrorMessage('Job complete error: ' + e.error.code + ' detail: ' + e.error.message, null);
            });
    }

}

customElements.define('zeebe-job-complete-dialog', ZeebeJobCompleteDialog);