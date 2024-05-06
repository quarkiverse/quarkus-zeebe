import { LitElement, html, css} from 'lit';
import { JsonRpc } from 'jsonrpc';
import {choose} from 'lit/directives/choose.js';
import '@vaadin/tabs';
import './zeebe/zeebe-processes.js';
import './zeebe/zeebe-process.js';
import './zeebe/zeebe-instances.js';
import './zeebe/zeebe-instance.js';


export class ZeebeDashboard extends LitElement {

    jsonRpc = new JsonRpc(this);

    static properties = {
        _context: { state: true},
        _nav: { state: true},
        _tab: { state: true},
        _tabs: {},
    };

    constructor() {
        super();
        this._tabs = new Map([
            ["processes", 0], ["process", 0],
            ["instances", 1], ["instance", 1],
            ["incidents", 2], ["jobs", 3], ["messages", 4], ["signals", 5], ["errors", 6]
        ]);
        this._navbar("processes");
    }

    render() {
        return html`
            <vaadin-tabs theme="equal-width-tabs" selected="${this._tab}">
                <vaadin-tab id="processes" @click=${() => this._navbar("processes")}>Processes</vaadin-tab>
                <vaadin-tab id="instances" @click=${() => this._navbar("instances")}>Instances</vaadin-tab>
                <vaadin-tab id="incidents" @click=${() => this._navbar("incidents")}>Incidents</vaadin-tab>
                <vaadin-tab id="jobs" @click=${() => this._navbar("jobs")}>Jobs</vaadin-tab>
                <vaadin-tab id="messages" @click=${() => this._navbar("messages")}>Messages</vaadin-tab>
                <vaadin-tab id="signals" @click=${() => this._navbar("signals")}>Signals</vaadin-tab>
                <vaadin-tab id="errors" @click=${() => this._navbar("errors")}>Errors</vaadin-tab>
            </vaadin-tabs>
            <div style="padding-bottom: 5px"></div>
            ${choose(this._nav, [
                ['processes', () => html`<zeebe-processes .context=${this._context} .navigation=${(request) => this.navigation(request)}></zeebe-processes>`], 
                ['process', () => html`<zeebe-process .context=${this._context} .navigation=${(request) => this.navigation(request)}></zeebe-process>`],
                ['instances', () => html`<zeebe-instances .context=${this._context} .navigation=${(request) => this.navigation(request)}></zeebe-instances>`], 
                ['instance', () => html`<zeebe-instance .context=${this._context} .navigation=${(request) => this.navigation(request)}></zeebe-instance>`]
            ],
            () => html`<p>Not defined ${this._nav}</p>`)}
        `;
    }

    _navbar(nav) {
        this.navigation({nav: nav, id: null});
    }

    navigation(request) {
        this._context = { nav: request.nav, id: request.id, extension: this.jsonRpc.getExtensionName() };
        this._tab = this._tabs.get(request.nav);
        this._nav = request.nav;
    }
}

customElements.define('qwc-zeebe-dashboard', ZeebeDashboard);