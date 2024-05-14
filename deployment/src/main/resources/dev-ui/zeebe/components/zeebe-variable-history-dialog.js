import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';
import './zeebe-table.js';

export class ZeebeVariableHistoryDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _variables: { state: true },
        _searchBar: {},
    }

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
        this._variables = []
        this._searchBar = false;
    }

    open(variables) {
        this._variables = variables;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="variable-edit-dialog" header-title="Variable history log" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._item])}
                           ${dialogFooterRenderer(this._footer, [])}
            >
            </vaadin-dialog>            
        `;
    }

    _render() {
        return html`
            <vaadin-vertical-layout style="align-items: stretch; width:100%; min-width: 400px; min-height: 200px; max-height: 600px;">
                <zeebe-table id="instance-variables-history-table" .withoutSearchBar=${this._variables}>
                    <vaadin-grid-column header="Time" path="data.time" auto-width></vaadin-grid-column>
                    <vaadin-grid-column header="Value" path="record.value.value"></vaadin-grid-column>
                </zeebe-table>
            </vaadin-vertical-layout>
        `;
    }

    _footer = () => html`
        <vaadin-button theme="tertiary" @click="${this._close}">Close</vaadin-button>
    `;

    _close() {
        this._opened = false
    }

}

customElements.define('zeebe-variable-history-dialog', ZeebeVariableHistoryDialog);