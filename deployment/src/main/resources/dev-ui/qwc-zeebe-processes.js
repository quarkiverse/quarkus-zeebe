import { LitElement, html, css} from 'lit';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/grid';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import './bpmnjs/qwc-zeebe-diagram.js';

export class ZeebeProcesses extends LitElement {

    jsonRpc = new JsonRpc(this);

    static styles = css`
        .arctable {
            height: 100%;
            padding-bottom: 10px;
        }

        a {
            cursor: pointer;
            color: var(--quarkus-blue);
        }
    `;

    static properties = {
        _items: {state: true},
        _item: {state: true},
        _xml: {state: true},
    };

    constructor() {
        super();
        this._fetchData();

        this._observer = this.jsonRpc.notifications().onNext(eventResponse => {
            this._fetchData();
        });

    }

    connectedCallback() {
        super.connectedCallback();
    }

    disconnectedCallback() {
        this._observer.cancel();
        super.disconnectedCallback()
    }

    _fetchData() {
        this.jsonRpc.processes()
            .then(itemResponse => {
                this._items = itemResponse.result;
            });
    }

    render() {
        if (this._item == null) {
            return this._table();
        }
        return this._process();
    }

    _table() {
        return html`
            <vaadin-grid .items="${this._items}" class="arctable" theme="no-border">
                <vaadin-grid-column header="Process Definition Key"
                                    ${columnBodyRenderer(this._definitionKeyRenderer, [])}
                                    resizable></vaadin-grid-column>
                <vaadin-grid-column header="BPMN Process Id" ${columnBodyRenderer(this._bpmnProcessIdRenderer, [])}
                                    resizable></vaadin-grid-column>
                <vaadin-grid-column header="Version" ${columnBodyRenderer(this._versionRenderer, [])}
                                    resizable></vaadin-grid-column>
                <vaadin-grid-column header="Deployment time" ${columnBodyRenderer(this._deploymentTimeRenderer, [])}
                                    resizable></vaadin-grid-column>
            </vaadin-grid>
        `;
    }

    _definitionKeyRenderer(item) {
        return html`
            <a @click=${() => this._fetchProcess(item)}>${item.id}</a>
        `;
    }

    _bpmnProcessIdRenderer(item) {
        return html`${item.record.value.bpmnProcessId}`;
    }

    _versionRenderer(item) {
        return html`${item.record.value.version}`;
    }

    _deploymentTimeRenderer(item) {
        return html`${item.data.time}`;
    }

    _fetchProcess(item) {
        this.jsonRpc.xml({id: item.id})
            .then(itemResponse => {
                this._item = item;
                this._xml = itemResponse.result;
            });
    }

    _process() {
        return html`
            <a @click=${() => this._item = null}>&lt; Back</a>
            <qwc-zeebe-diagram xml="${this._xml}"></qwc-zeebe-diagram>
        `;
    }
}

customElements.define('qwc-zeebe-processes', ZeebeProcesses);