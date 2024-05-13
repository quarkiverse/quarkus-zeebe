import { LitElement, html } from 'lit';
import { dialogRenderer, dialogFooterRenderer } from '@vaadin/dialog/lit.js';

export class ZeebeUserTasksInfoDialog extends LitElement {

    static properties = {
        _opened: { state: true },
        context: {},
        _item: { state: true },
    }

    connectedCallback() {
        super.connectedCallback();
        this._opened = false;
    }

    open(item) {
        this._item = item;
        this._opened = true;
    }

    render() {
        return html`
            <vaadin-dialog id="user-task-complete-dialog" header-title="User Task details" .opened=${this._opened}
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
                <vaadin-text-field label="Process Instance Key" value="${this._item.record.value.processInstanceKey}" readonly></vaadin-text-field>
                <vaadin-text-field label="Element Id" value="${this._item.record.value.elementId}" readonly ></vaadin-text-field>
                
                <vaadin-text-field label="Status" value="${this._item.record.intent}" readonly ></vaadin-text-field>
                <vaadin-text-field label="Due Date" value="${this._item.data.dueDate}" readonly ></vaadin-text-field>
                <vaadin-text-field label="Created" value="${this._item.data.created}" readonly ></vaadin-text-field>
                <vaadin-text-field label="Updated" value="${this._item.data.time}" readonly ></vaadin-text-field>
                <vaadin-text-field label="Assignee" value="${this._item.data.assignee}" readonly ></vaadin-text-field>
                <vaadin-text-field label="Candidate Groups" colspan="2" value="${this._item.data.groups}" readonly ></vaadin-text-field>
                <vaadin-text-field label="Candidate Users" colspan="2" value="${this._item.data.users}" readonly ></vaadin-text-field>
                <vaadin-text-area colspan="2"
                        style="width:100%; min-width: 400px; min-height: 100px; max-height: 300px;"
                        label="User Task variables"
                        helper-text="Variables in JSON format"
                        placeholder='Input variables'
                        readonly
                        value="${JSON.stringify(this._item.record.value.variables)}"></vaadin-text-area>
                
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

customElements.define('zeebe-user-task-info-dialog', ZeebeUserTasksInfoDialog);