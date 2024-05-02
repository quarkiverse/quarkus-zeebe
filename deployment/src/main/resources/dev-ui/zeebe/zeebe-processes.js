import { LitElement, html, css} from 'lit';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/grid';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';

export class ZeebeProcesses extends LitElement {

    static styles = css`
        .arctable {
            height: 100%;
            padding-bottom: 10px;
        }
        .flex-auto {
            flex: 1 1 auto;
        }
        a {
            cursor: pointer;
            color: var(--quarkus-blue);
        }
    `;

    static properties = {
        _items: {state: true},
        _filteredItems: {state: true},
        navigation: {},
    };

    connectedCallback() {
        super.connectedCallback();
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
        this._items = [];
        this.jsonRpc.processes()
            .then(itemResponse => {
                this._items = itemResponse.result.map((item) => ({
                    ...item,
                    key: `${item.id}`,
                    bpmnProcessId: `${item.record.value.bpmnProcessId}`,
                    version: `${item.record.value.version}`,
                    time: `${item.data.time}`,
                    active: `${item.data.active}`,
                    ended: `${item.data.ended}`,
                }));
                this._filteredItems = this._items;
            });
    }

    render() {
        return html`
            <vaadin-horizontal-layout theme="spacing padding"  style="align-items: stretch">
                <vaadin-text-field style="align-self: start" placeholder="Search" @value-changed=${this._searchTable}>
                    <vaadin-icon slot="prefix" icon="font-awesome-solid:magnifying-glass"></vaadin-icon>
                </vaadin-text-field>
                <div class="flex-auto"></div>
                <vaadin-button style="align-self: end">
                    <vaadin-icon slot="prefix" icon="font-awesome-solid:cloud-arrow-up"></vaadin-icon>
                    Deploy process
                </vaadin-button>
            </vaadin-horizontal-layout>

            <vaadin-grid .items="${this._filteredItems}" class="arctable" theme="no-border">
                <vaadin-grid-column header="Process Definition Key" ${columnBodyRenderer(this._definitionKeyRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="BPMN Process Id" path="bpmnProcessId" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Version" path="version"></vaadin-grid-column>
                <vaadin-grid-column header="#Active" path="active"></vaadin-grid-column>
                <vaadin-grid-column header="#Ended" path="ended"></vaadin-grid-column>
                <vaadin-grid-column header="Deployment time" path="time" resizable></vaadin-grid-column>
            </vaadin-grid>
        `;
    }

    _searchTable(e) {
        const searchTerm = (e.detail.value || '').trim();
        const matchesTerm = (value) => value.toLowerCase().includes(searchTerm.toLowerCase());
        this._filteredItems = this._items.filter(
            ({ key, bpmnProcessId }) =>
                !searchTerm ||
                matchesTerm(key) ||
                matchesTerm(bpmnProcessId)
        );
    }

    _definitionKeyRenderer(item) {
        return html`
            <a @click=${() => this.navigation({ nav: "process", id: item.id})}>${item.id}</a>
        `;
    }
}

customElements.define('zeebe-processes', ZeebeProcesses);