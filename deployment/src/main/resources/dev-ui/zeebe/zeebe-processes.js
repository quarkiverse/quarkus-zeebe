import { LitElement, html } from 'lit';
import { JsonRpc } from 'jsonrpc';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import {ref, createRef} from 'lit/directives/ref.js';
import './components/zeebe-table.js';
import './components/zeebe-process-deploy-dialog.js';

export class ZeebeProcesses extends LitElement {

    static properties = {
        _items: {state: true},
        navigation: {},
        _deployDialogOpened: { state: true},
    };

    _processDeployDialogRef = createRef();

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
                <vaadin-button slot="toolbar" theme="primary" style="align-self: end" @click=${() => this._processDeployDialogRef.value.open()}>
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
            <zeebe-process-deploy-dialog ${ref(this._processDeployDialogRef)}></zeebe-process-deploy-dialog>
        `;
    }

    _definitionKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "process", id: item.id})}>${item.id}</a>
        `;
    }
}

customElements.define('zeebe-processes', ZeebeProcesses);