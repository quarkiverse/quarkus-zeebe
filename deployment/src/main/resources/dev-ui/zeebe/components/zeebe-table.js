import { LitElement, html, css } from 'lit';
import '@vaadin/grid';

export class ZeebeTable extends LitElement {

    static styles = css`
        .table {
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
    };

    set items(val) {
        this._items = val;
        this._filteredItems = this._items;
    }

    connectedCallback() {
        super.connectedCallback();
    }

    disconnectedCallback() {
        super.disconnectedCallback()
    }

    render() {
        return html`
            <vaadin-horizontal-layout theme="spacing padding"  style="align-items: stretch">
                <vaadin-text-field style="align-self: start" placeholder="Search" @value-changed=${this._searchTable}>
                    <vaadin-icon slot="prefix" icon="font-awesome-solid:magnifying-glass"></vaadin-icon>
                </vaadin-text-field>
                <div class="flex-auto"></div>
                <slot name="toolbar"></slot>
            </vaadin-horizontal-layout>
            
            <vaadin-grid .items="${this._filteredItems}" class="table" theme="no-border">
                <slot></slot>
            </vaadin-grid>
        `;
    }

    _searchTable(e) {
        if (!this._items || this._items.length <= 0) {
            return;
        }

        const searchTerm = (e.detail.value || '').trim();
        const matchesTerm = (value) => value.toLowerCase().includes(searchTerm.toLowerCase());
        this._filteredItems = this._items.filter(({ searchTerms }) => matchesTerm(searchTerms));
    }
}

customElements.define('zeebe-table', ZeebeTable);