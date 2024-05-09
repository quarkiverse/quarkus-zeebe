import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import { dialogRenderer, dialogHeaderRenderer } from '@vaadin/dialog/lit.js';
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

        this._observer = this.jsonRpc.notifications().onNext(response => {
            if (response.result.type === 'PROCESS') {
                if (response.result.data.type === 'DEPLOYED') {
                    this._fetchData();
                }
            }
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
            <zeebe-table id="processes-table" .items=${this._items}>
                <vaadin-button slot="toolbar" theme="primary" style="align-self: end" @click=${() => this._deployDialogOpened = true}>
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
            <vaadin-dialog header-title="Deploy process to Zeebe" .opened=${this._deployDialogOpened}
               @opened-changed=${(event) => { this._deployDialogOpened = event.detail.value; }}
               ${dialogHeaderRenderer(
                       () => html`
                           <vaadin-icon @click=${() => {this._deployDialogOpened = false}} icon="font-awesome-solid:xmark"></vaadin-icon>
                       `, [] )}                           
               ${dialogRenderer(() => html`
                <p>Accepted file formats: BPMN (.bpmn)</p>
                <vaadin-upload id="deploy-process" nodrop style="width: 400px; max-width: 100%; align-items: stretch;"
                               accept=".bpmn"
                               method="POST"
                               target="/q/zeebe/ui/cmd/process-deploy"
                               @upload-success=${(e) => { 
                                   document.getElementById("deploy-process").files = [];
                                   this._deployDialogOpened = false
                               }}
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