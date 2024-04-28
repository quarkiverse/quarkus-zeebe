import { LitElement, html, css} from 'lit';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/grid';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import 'qui-badge';

export class ZeebeMonitorInstances extends LitElement {

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
        _instances: {state: true},
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
        this.jsonRpc.instances()
            .then(itemResponse => {
               this._instances = itemResponse.result;
           });
    }

    render() {
        return html`
            <vaadin-grid .items="${this._instances}" class="arctable" theme="no-border">
                <vaadin-grid-column header="Process Instance Key" ${columnBodyRenderer(this._idRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="Process Id" ${columnBodyRenderer(this._processIdRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="Process Key" ${columnBodyRenderer(this._processKeyRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="State" ${columnBodyRenderer(this._processStateRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="Start time" ${columnBodyRenderer(this._processStartTimeRenderer, [])} resizable></vaadin-grid-column>
                <vaadin-grid-column header="End time" ${columnBodyRenderer(this._processEndTimeRenderer, [])} resizable></vaadin-grid-column>
            </vaadin-grid>
        `;
    }

    _idRenderer(item) {
      return html`${item.id}`;
    }

    _processIdRenderer(item) {
      return html`${item.record.value.bpmnProcessId}`;
    }

    _processKeyRenderer(item) {
      return html`${item.record.value.processDefinitionKey}`;
    }

    _processStateRenderer(item) {
      return html`
        <qui-badge level="success" primary><span>${item.data.state}</span></qui-badge>
        `;
    }

    _processStartTimeRenderer(item) {
      return html`${item.data.start}`;
    }

    _processEndTimeRenderer(item) {
      return html`${item.data.end}`;
    }
}
customElements.define('qwc-zeebe-monitor-instances', ZeebeMonitorInstances);