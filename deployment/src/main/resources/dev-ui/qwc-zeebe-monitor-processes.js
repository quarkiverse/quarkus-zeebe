import { LitElement, html, css} from 'lit';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/grid';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';

export class ZeebeMonitorProcesses extends LitElement {

    jsonRpc = new JsonRpc(this);

    static styles = css`
        .arctable {
          height: 100%;
          padding-bottom: 10px;
        }
        a {
            cursor:pointer;
            padding-left: 10px;
        }
        .buttonBar {
            display: flex;
            justify-content: space-between;
            gap: 10px;
            align-items: center;
            width: 90%;
        }

        .buttonBar .button {
            width: 100%;
        }
    `;

    static properties = {
        _items: {state: true},
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
        return html`
            <vaadin-grid .items="${this._items}" class="arctable" theme="no-border">
                <vaadin-grid-column header="Process Definition Key" ${columnBodyRenderer(this._definitionKeyRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="BPMN Process Id" ${columnBodyRenderer(this._bpmnProcessIdRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="Version" ${columnBodyRenderer(this._versionRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="Deployment time" ${columnBodyRenderer(this._deploymentTimeRenderer, [])} resizable></vaadin-grid-column>
            </vaadin-grid>
        `;
    }

    _definitionKeyRenderer(item) {
      return html`${item.id}`;
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

}
customElements.define('qwc-zeebe-monitor-processes', ZeebeMonitorProcesses);