import { LitElement, html, css} from 'lit';
import '@vaadin/tabs';
import './zeebe/zeebe-body.js';
import { JsonRpc } from 'jsonrpc';

export class ZeebeDashboard extends LitElement {

    jsonRpc = new JsonRpc(this);

    static properties = {
        _nav: {state: true},
    };

    constructor() {
        super();
        this._nav = "processes";
    }

    render() {
        return html`
            <vaadin-tabs theme="equal-width-tabs">
                <vaadin-tab id="process-info" @click=${() => this._navbar("processes")}>Processes</vaadin-tab>
                <vaadin-tab id="process-instances" @click=${() => this._navbar("instances")}>Instances</vaadin-tab>
                <vaadin-tab id="process-incidents" @click=${() => this._navbar("incidents")}>Incidents</vaadin-tab>
                <vaadin-tab id="process-jobs" @click=${() => this._navbar("jobs")}>Jobs</vaadin-tab>
                <vaadin-tab id="process-messages" @click=${() => this._navbar("Messages")}>Messages</vaadin-tab>
                <vaadin-tab id="process-signals" @click=${() => this._navbar("signals")}>Signals</vaadin-tab>
                <vaadin-tab id="process-errors" @click=${() => this._navbar("errors")}>Errors</vaadin-tab>
            </vaadin-tabs>
            <zeebe-body .nav=${this._nav} .extension=${this.jsonRpc.getExtensionName()}></zeebe-body>
        `;
    }

    _navbar(item) {
        this._nav = item;
    }

}

customElements.define('qwc-zeebe-dashboard', ZeebeDashboard);