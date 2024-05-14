import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';
import './zeebe-table.js';

export class ZeebeIncidentDetailsDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _item: { state: true },
    }

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
        this._item = null
    }

    open(item) {
        this._item = item;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="incident-details-dialog" header-title="Incident details" .opened=${this._opened}
                           @opened-changed=${(e) => {this._opened = e.detail.value;}}
                           ${dialogRenderer(() => this._render(), [this._item])}
                           ${dialogFooterRenderer(this._footer, [])}
            >
            </vaadin-dialog>            
        `;
    }

    _responsiveSteps = [
        { minWidth: 0, columns: 1 },
        { minWidth: '500px', columns: 2 },
    ];

    _render() {
        return html`
            <vaadin-vertical-layout style="align-items: stretch; max-width: 700px; min-width: 200px; min-height: 200px; max-height: 600px;">
                <vaadin-form-layout .responsiveSteps="${this._responsiveSteps}">
                    <vaadin-text-field label="Element Id" value="${this._item.item.record.value.elementId}" readonly></vaadin-text-field>
                    <vaadin-text-field label="Incident Key" value="${this._item.item.record.key}" readonly></vaadin-text-field>
                    <vaadin-text-field label="Job Key" value="${this._item.item.record.value.jobKey > 0 ? this._item.item.record.value.jobKey : ''}" readonly></vaadin-text-field>
                    <vaadin-text-field label="Type" value="${this._item.item.record.value.errorType}" readonly></vaadin-text-field>
                    <vaadin-text-field label="Status" value="${this._item.item.record.intent}" readonly></vaadin-text-field>
                    <vaadin-text-field label="Create" value="${this._item.item.data.created}" readonly></vaadin-text-field>
                    <vaadin-text-field label="Resolved" value="${this._item.item.data.resolved}" readonly></vaadin-text-field>
                    <vaadin-text-area
                            colspan="2"
                            readonly
                            style="width:100%; min-width: 400px; min-height: 100px; max-height: 300px;"
                            label="Error message"
                            value="${this._item.item.record.value.errorMessage}"
                    ></vaadin-text-area>
                </vaadin-form-layout>
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

customElements.define('zeebe-incident-details-dialog', ZeebeIncidentDetailsDialog);