import { JsonRpc } from 'jsonrpc';
import { LitElement, html} from 'lit';
import {when} from 'lit/directives/when.js';
import './bpmnjs/zeebe-bpmn-diagram.js';
import '@vaadin/tabs';
import '@vaadin/grid';
import '@vaadin/tabsheet';
import '@vaadin/form-layout';
import '@vaadin/text-field';

export class ZeebeProcess extends LitElement {

    static properties = {
        _item: {},
        _xml: {state: true},
        context: {},
        navigation: {},
    };

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this.jsonRpc.process({id: this.context.id})
            .then(itemResponse => {
                this._item = itemResponse.result;
                return this.jsonRpc.xml({id: this.context.id})
                    .then(itemResponse => {
                        this._xml = itemResponse.result;
                    });
            });

    }

    render() {
        return html`
            ${when(this._xml, 
                    () => html`<zeebe-bpmn-diagram id="diagram" .xml="${this._xml}" .data=${this._item.data}></zeebe-bpmn-diagram>`,
                    () => html`<p style="position: relative; overflow: hidden; width: 100%; height: 100%;"></p>`
            )}
            ${when(this._item,
                    () => this._body(),
                    () => html`<p style="position: relative; overflow: hidden; width: 100%; height: 100%;">Loading...</p>`
            )}
        `;
    }

    _body() {
        return html`
            <vaadin-tabsheet>
                <vaadin-tabs slot="tabs">
                    <vaadin-tab id="process-info" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:circle-info"></vaadin-icon>
                        <span>Details</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-instances" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Instances</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-messages" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:envelope"></vaadin-icon>
                        <span>Message subscriptions</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-signals" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:envelope"></vaadin-icon>
                        <span>Signal subscriptions</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-timers" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Timers</span>
                    </vaadin-tab>
                </vaadin-tabs>

                <div tab="process-info">
                    <vaadin-form-layout>
                        <vaadin-text-field readonly label="Key" value="${this._item.id}"></vaadin-text-field>
                        <vaadin-text-field readonly label="BPMN process id" value="${this._item.record.value.bpmnProcessId}"></vaadin-text-field>
                        <vaadin-text-field readonly label="Version" value="${this._item.record.value.version}"></vaadin-text-field>
                        <vaadin-text-field readonly label="Deploy time" value="${this._item.data.time}"></vaadin-text-field>
                    </vaadin-form-layout>
                </div>
                <div tab="process-instances">2 This is the Dashboard tab content</div>
                <div tab="process-messages">3 This is the Dashboard tab content</div>
                <div tab="process-signals">4 This is the Dashboard tab content</div>
                <div tab="process-timers">5 This is the Dashboard tab content</div>

            </vaadin-tabsheet>            
        `;
    }
}

customElements.define('zeebe-process', ZeebeProcess);