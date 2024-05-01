import { LitElement, html, css} from 'lit';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/grid';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import './zeebe-instance.js';

export class ZeebeInstances extends LitElement {

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
        _item: {state: true},
        _xml: {state: true},
        extension: {type: String},
    };

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.extension);
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
        this.jsonRpc.instances()
            .then(itemResponse => {
                this._items = itemResponse.result.map((item) => ({
                    ...item,
                    key: `${item.id}`,
                    version: `${item.record.value.version}`,
                    state: `${item.data.state}`,
                    bpmnProcessId: `${item.record.value.bpmnProcessId}`,
                    processKey: `${item.record.value.processDefinitionKey}`,
                    start: `${item.data.start}`,
                    end: `${item.data.end}`,
                }));
                this._filteredItems = this._items;
            });
    }

    render() {
        if (this._item == null) {
            return this._table();
        }
        return html`
            <vaadin-horizontal-layout theme="spacing-xs padding">
                <a @click=${() => this._item = null}>Instances</a>
                <div>/</div>
                <div>${this._item.id}</div>
            </vaadin-horizontal-layout>

            <zeebe-instance id="instance" .item=${this._item} .xml=${this._xml} .extension=${this.extension}></zeebe-instance>
        `;
    }

    _table() {
        return html`
            <vaadin-horizontal-layout theme="spacing padding"  style="align-items: stretch">
                <vaadin-text-field style="align-self: start" placeholder="Search" @value-changed=${this._searchTable}>
                    <vaadin-icon slot="prefix" icon="font-awesome-solid:magnifying-glass"></vaadin-icon>
                </vaadin-text-field>
            </vaadin-horizontal-layout>
            
            <vaadin-grid .items="${this._filteredItems}" class="arctable" theme="no-border">
                <vaadin-grid-column header="Process Instance Key" ${columnBodyRenderer(this._definitionKeyRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="Process Id" path="bpmnProcessId" resizable></vaadin-grid-column>
                <vaadin-grid-column header="Process key" path="processKey"></vaadin-grid-column>
                <vaadin-grid-column header="State" path="state"></vaadin-grid-column>
                <vaadin-grid-column header="Start time" path="start" resizable></vaadin-grid-column>
                <vaadin-grid-column header="End time" path="end" resizable></vaadin-grid-column>
            </vaadin-grid>
        `;
    }

    _searchTable(e) {
        const searchTerm = (e.detail.value || '').trim();
        const matchesTerm = (value) => value.toLowerCase().includes(searchTerm.toLowerCase());
        this._filteredItems = this._items.filter(
            ({ key, bpmnProcessId, processKey }) =>
                !searchTerm ||
                matchesTerm(key) ||
                matchesTerm(bpmnProcessId) ||
                matchesTerm(processKey)
        );
    }

    _definitionKeyRenderer(item) {
        return html`
            <a @click=${() => this._fetchProcess(item)}>${item.id}</a>
        `;
    }

    _fetchProcess(item) {
        this.jsonRpc.xml({id: item.record.value.processDefinitionKey})
            .then(itemResponse => {
                this._xml = itemResponse.result;
                this._item = item;
            });
    }

}

customElements.define('zeebe-instances', ZeebeInstances);