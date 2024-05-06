import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import { dialogRenderer } from '@vaadin/dialog/lit.js';
import '@vaadin/upload';
import './components/zeebe-table.js';

export class ZeebeProcesses extends LitElement {

    static properties = {
        _items: {state: true},
        navigation: {},
        _deployDialogOpened: { state: true},
    };

    connectedCallback() {
        super.connectedCallback();
        this._deployDialogOpened = false;
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._fetchData();

        this._observer = this.jsonRpc.notifications().onNext(eventResponse => {
            this._fetchData();
        });
    }

    disconnectedCallback() {
        this._observer.cancel();
        super.disconnectedCallback()
    }

    _fetchData() {
        this.jsonRpc.processes()
            .then(itemResponse => {
                this._items = itemResponse.result.map((item) => ({
                    ...item,
                    searchTerms: `${item.id} ${item.record.value.bpmnProcessId}`,
                }));
            });
    }

    render() {
        return html`
            <zeebe-table .items=${this._items}>
                <vaadin-button slot="toolbar" style="align-self: end" @click=${() => this._deployDialogOpened = true}>
                    <vaadin-icon slot="prefix" icon="font-awesome-solid:cloud-arrow-up"></vaadin-icon>
                    Deploy process
                </vaadin-button>
                
                <vaadin-grid-column header="Process Definition Key" ${columnBodyRenderer(this._definitionKeyRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="BPMN Process Id" path="record.value.bpmnProcessId" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Version" path="record.value.version"></vaadin-grid-column>
                <vaadin-grid-column header="#Active" path="data.active"></vaadin-grid-column>
                <vaadin-grid-column header="#Ended" path="data.ended"></vaadin-grid-column>
                <vaadin-grid-column header="Deployment time" path="data.time" resizable></vaadin-grid-column>
            </zeebe-table>
            <vaadin-dialog header-title="Deploy process" .opened=${this._deployDialogOpened}
               @opened-changed=${(event) => {
                   this._deployDialogOpened = event.detail.value;
               }}
               ${dialogRenderer(() => html`
                <vaadin-upload id="upload-drop-disabled" nodrop no-auto
                               method="POST"
                               target="/q/zeebe/ui/process"
                ></vaadin-upload>
               `, [])}
            >
                
            </vaadin-dialog>
        `;
    }

    _definitionKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "process", id: item.id})}>${item.id}</a>
        `;
    }
}

customElements.define('zeebe-processes', ZeebeProcesses);