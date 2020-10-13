package mocks;

import jade.core.*;
import jade.domain.AMSEventQueueFeeder;
import jade.lang.acl.ACLMessage;
import jade.mtp.MTPDescriptor;
import jade.security.Credentials;
import jade.security.JADEPrincipal;
import jade.security.JADESecurityException;
import jade.wrapper.ContainerController;
import jade.wrapper.ContainerProxy;

import java.util.HashSet;
import java.util.Set;

public class ContainerControllerMock extends ContainerController {
    private ContainerControllerMock(ContainerProxy cp,
                                    AgentContainer impl,
                                    String platformName) {
        super(cp, impl, platformName);
    }

    public ContainerControllerMock() {
        this(createContainerProxyMock(),
                createAgentContainerMock(),
                createPlatformName());
    }

    private static ContainerProxy createContainerProxyMock() {
        return new ContainerProxy() {
            @Override
            public void createAgent(AID aid, String s, Object[] objects) throws Throwable {
            }

            @Override
            public void killContainer() throws Throwable {
            }

            @Override
            public MTPDescriptor installMTP(String s, String s1) throws Throwable {
                return null;
            }

            @Override
            public void uninstallMTP(String s) throws Throwable {

            }

            @Override
            public void suspendAgent(AID aid) throws Throwable {

            }

            @Override
            public void activateAgent(AID aid) throws Throwable {

            }

            @Override
            public void killAgent(AID aid) throws Throwable {

            }

            @Override
            public void moveAgent(AID aid, Location location) throws Throwable {

            }

            @Override
            public void cloneAgent(AID aid, Location location, String s) throws Throwable {

            }
        };
    }

    private static AgentContainer createAgentContainerMock() {
        return new AgentContainer() {
            private final Set<AID> agentsIds = new HashSet<>();

            @Override
            public void initAgent(AID aid, Agent agent, JADEPrincipal jadePrincipal, Credentials credentials)
                    throws NameClashException, IMTPException, NotFoundException, JADESecurityException {
                if (!agentsIds.add(aid)) {
                    throw new NameClashException();
                }
            }

            @Override
            public AID getAMS() {
                return null;
            }

            @Override
            public AID getDefaultDF() {
                return null;
            }

            @Override
            public ContainerID getID() {
                return null;
            }

            @Override
            public String getPlatformID() {
                return null;
            }

            @Override
            public MainContainer getMain() {
                return null;
            }

            @Override
            public ServiceFinder getServiceFinder() {
                return null;
            }

            @Override
            public boolean isJoined() {
                return false;
            }

            @Override
            public ServiceManager getServiceManager() {
                return null;
            }

            @Override
            public NodeDescriptor getNodeDescriptor() {
                return new NodeDescriptor();
            }

            @Override
            public void powerUpLocalAgent(AID aid) throws NotFoundException {

            }

            @Override
            public Agent addLocalAgent(AID aid, Agent agent) {
                return null;
            }

            @Override
            public void removeLocalAgent(AID aid) {

            }

            @Override
            public boolean isLocalAgent(AID aid) {
                return false;
            }

            @Override
            public Agent acquireLocalAgent(AID aid) {
                return null;
            }

            @Override
            public void releaseLocalAgent(AID aid) {

            }

            @Override
            public AID[] agentNames() {
                return new AID[0];
            }

            @Override
            public void fillListFromMessageQueue(jade.util.leap.List list, Agent agent) {

            }

            @Override
            public void fillListFromReadyBehaviours(jade.util.leap.List list, Agent agent) {

            }

            @Override
            public void fillListFromBlockedBehaviours(jade.util.leap.List list, Agent agent) {

            }

            @Override
            public void becomeLeader(AMSEventQueueFeeder amsEventQueueFeeder) {

            }

            @Override
            public void addAddressToLocalAgents(String s) {

            }

            @Override
            public void removeAddressFromLocalAgents(String s) {

            }

            @Override
            public boolean postMessageToLocalAgent(ACLMessage aclMessage, AID aid) {
                return false;
            }

            @Override
            public boolean postMessagesBlockToLocalAgent(ACLMessage[] aclMessages, AID aid) {
                return false;
            }

            @Override
            public Location here() {
                return null;
            }

            @Override
            public void shutDown() {

            }
        };
    }

    private static String createPlatformName() {
        return "test-platform";
    }
}
