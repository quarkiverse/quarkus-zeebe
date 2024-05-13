import { JsonRpc } from 'jsonrpc';
import { LitElement, html, css } from 'lit';
import './bpmnjs/zeebe-bpmn-diagram.js';
import '@vaadin/tabs';
import '@vaadin/grid';
import '@vaadin/tabsheet';
import '@vaadin/form-layout';
import '@vaadin/text-field';

export class ZeebeInstance extends LitElement {

    static styles = css`
        .link > input {
            cursor: pointer;
            color: var(--quarkus-blue);
        }        
    `;

    static properties = {
        _item: {state: true},
        context: {},
        navigation: {},
    };

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.context.extension);
        this._observer = this.jsonRpc.notifications().onNext(response => {
            this._fetchData();
            if (response.result.event === 'PROCESS_INSTANCE') {
                if (response.result.data.processInstanceKey === this._item.item.id) {
                    this._fetchData();
                }
            }
        });

        this.jsonRpc.instance({id: this.context.id})
            .then(itemResponse => {
                this._item = itemResponse.result;
            });

    }

    render() {
        if (this._item) {
            return this._body();
        } else {
            return html`<p style="position: relative; overflow: hidden; width: 100%; height: 100%;"></p>`
        }
    }

    detailsColumn = [{ minWidth: 0, columns: 1 },{ minWidth: '600px', columns: 2 }, { minWidth: '1280px', columns: 3 }];

    _body() {
        return html`
            <zeebe-bpmn-diagram id="diagram" .xml="${this._item.xml}" .data=${this._item.diagram}></zeebe-bpmn-diagram>
            <vaadin-tabsheet>
                <vaadin-tabs slot="tabs">
                    <vaadin-tab id="process-info" theme="icon">
                        <vaadin-icon icon="font-awesome-solid:circle-info"></vaadin-icon>
                        <span>Details</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-variables" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Variables</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-audit" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Audit log</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-incidents" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Incidents</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-jobs" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>Jobs</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-user-tasks" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:object-group"></vaadin-icon>
                        <span>User tasks</span>
                    </vaadin-tab>                    
                    <vaadin-tab id="process-messages" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:envelope"></vaadin-icon>
                        <span>Messages</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-escalation" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:envelope"></vaadin-icon>
                        <span>Escalation</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-timers" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Timers</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-called-instances" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Called instances</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-errors" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Errors</span>
                    </vaadin-tab>
                    <vaadin-tab id="process-modify" theme="icon">
                        <vaadin-icon icon="font-awesome-regular:clock"></vaadin-icon>
                        <span>Modify</span>
                    </vaadin-tab>                    
                </vaadin-tabs>

                <div tab="process-info">
                    <vaadin-form-layout .responsiveSteps="${this.detailsColumn}">
                        <vaadin-text-field readonly label="Key" value="${this._item.item.id}"></vaadin-text-field>
                        <vaadin-text-field readonly class="link" label="Process definition key"
                                           value="${this._item.item.record.value.processDefinitionKey}"
                                           @click=${() => this.navigation({ nav: "process", id: this._item.item.record.value.processDefinitionKey})}>
                        </vaadin-text-field>                        
                        <vaadin-text-field readonly label="BPMN process id" value="${this._item.item.record.value.bpmnProcessId}"></vaadin-text-field>
                        <vaadin-text-field readonly label="State" value="${this._item.item.data.state}"></vaadin-text-field>
                        <vaadin-text-field readonly label="Start time" value="${this._item.item.data.start}"></vaadin-text-field>
                        <vaadin-text-field readonly label="End time" value="${this._item.item.data.end}"></vaadin-text-field>
                        <vaadin-text-field readonly label="Version" value="${this._item.item.record.value.version}"></vaadin-text-field>
                    </vaadin-form-layout>
                </div>
                <div tab="process-variables">2 This is the Dashboard tab content</div>
                <div tab="process-audit">2 This is the Dashboard tab content</div>
                <div tab="process-incidents">2 This is the Dashboard tab content</div>
                <div tab="process-jobs">4 This is the Dashboard tab content</div>
                <div tab="process-user-tasks">5 This is the Dashboard tab content</div>
                <div tab="process-messages">3 This is the Dashboard tab content</div>
                <div tab="process-escalation">5 This is the Dashboard tab content</div>
                <div tab="process-timers">5 This is the Dashboard tab content</div>
                <div tab="process-called-instances">5 This is the Dashboard tab content</div>
                <div tab="process-errors">5 This is the Dashboard tab content</div>
                <div tab="process-modify">5 This is the Dashboard tab content</div>

            </vaadin-tabsheet>        
        `;
    }
}

customElements.define('zeebe-instance', ZeebeInstance);