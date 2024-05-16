import { JsonRpc } from 'jsonrpc';
import { notifier } from 'notifier';
import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';

export class ZeebeUserTasksCompleteDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _item: { state: true },
        _variables: { state: true },
    }

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
        this.jsonRpc = new JsonRpc(this.context.extension);
    }

    open(item) {
        this._item = item;
        this._variables = null;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="user-task-complete-dialog" header-title="Complete User Task" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._item, this._variables])}
                           ${dialogFooterRenderer(this._footer, [])}
            >
            </vaadin-dialog>            
        `;
    }

    _render() {
        return html`
            <vaadin-vertical-layout style="align-items: stretch; width:100%; min-width: 400px; min-height: 200px; max-height: 600px;">
                <vaadin-text-field label="Assignee" value="${this._item.data.assignee}" readonly ></vaadin-text-field>
                <vaadin-text-field label="Candidate Groups" value="${this._item.data.groups}" readonly ></vaadin-text-field>
                <vaadin-text-field label="Candidate Users" value="${this._item.data.users}" readonly ></vaadin-text-field>
                <vaadin-text-area
                        style="width:100%; min-width: 400px; min-height: 100px; max-height: 300px;"
                        label="User Task variables"
                        helper-text="Variables in JSON format"
                        placeholder='Input variables'
                        readonly
                        value="${JSON.stringify(this._item.record.value.variables)}"></vaadin-text-area>

                <vaadin-text-area
                        style="width:100%; min-width: 400px; min-height: 100px; max-height: 300px;"
                        label="Variables"
                        helper-text="Variables in JSON format"
                        placeholder='{"name":"value"}'
                        value="${this._variables}"
                        @value-changed=${(e) => {this._variables = e.detail.value;}}                        
                ></vaadin-text-area>
            </vaadin-vertical-layout>               
        `;
    }

    _footer = () => html`
        <vaadin-button theme="tertiary" @click="${this._close}">Cancel</vaadin-button>
        <vaadin-button theme="primary" @click=${this._action}>Complete</vaadin-button>        
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

        this.jsonRpc.userTaskComplete({key: this._item.record.key, variables: variables})
            .then(response => {
                console.log(response);
                this._close();
            })
            .catch(e => {
                console.log(e);
                notifier.showErrorMessage('Complete user task error: ' + e.error.code + ' detail: ' + e.error.message, null);
            });
    }

}

customElements.define('zeebe-user-task-complete-dialog', ZeebeUserTasksCompleteDialog);