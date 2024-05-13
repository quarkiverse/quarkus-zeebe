import { JsonRpc } from 'jsonrpc';
import { notifier } from 'notifier';
import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';


export class ZeebeInstanceCreateDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _key: { state: true },
        _variables: { state: true },
    }

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
        this.jsonRpc = new JsonRpc(this.context.extension);
    }

    open(key ) {
        this._key = key;
        this._variables = null;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="create-process-instance-dialog" header-title="Create new process instance" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._key, this._variables])}
                           ${dialogFooterRenderer(this._footer, [])}
            >
            </vaadin-dialog>
        `;
    }

    _footer = () => html`
        <vaadin-button theme="tertiary" @click="${this._close}">Cancel</vaadin-button>
        <vaadin-button theme="primary" @click=${this._action}>Create</vaadin-button>        
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
        this.jsonRpc.createProcessInstance({processDefinitionKey: this._key, variables: variables})
            .then(response => {
                console.log(response);
                this._close();
            })
            .catch(e => {
                console.log(e);
                notifier.showErrorMessage('Create process instance error: ' + e.error.code + ' message: ' + e.error.message, null)
            });
    }

    _render() {
        return html`
            <vaadin-text-area
                    style="width:100%; min-width: 400px; min-height: 200px; max-height: 400px;"
                    label="Variables"
                    helper-text="Variables in JSON format"
                    placeholder='{"name":"value"}'
                    value="${this._variables}"
                    @value-changed=${(e) => {this._variables = e.detail.value;}}                        
            ></vaadin-text-area>
        `;
    }

}

customElements.define('zeebe-instance-create-dialog', ZeebeInstanceCreateDialog);