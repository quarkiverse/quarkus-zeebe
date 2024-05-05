import { LitElement, html, css } from 'lit';

export class Table extends LitElement {

    static styles = css`
        .table {
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
    }

}