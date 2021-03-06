/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.thrift.server;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransportException;
import org.apache.log4j.Logger;

/**
 * Simple singlethreaded server for testing.
 *
 */
public class TSimpleServer extends TServer {

  private static final Logger LOGGER = Logger.getLogger(TSimpleServer.class.getName());

  private boolean stopped_ = false;

  public TSimpleServer(TProcessor processor,
                       TServerTransport serverTransport) {
    super(new TProcessorFactory(processor), serverTransport);
  }

  public TSimpleServer(TProcessor processor,
                       TServerTransport serverTransport,
                       TTransportFactory transportFactory,
                       TProtocolFactory protocolFactory) {
    super(new TProcessorFactory(processor), serverTransport, transportFactory, protocolFactory);
  }

  public TSimpleServer(TProcessor processor,
                       TServerTransport serverTransport,
                       TTransportFactory inputTransportFactory,
                       TTransportFactory outputTransportFactory,
                       TProtocolFactory inputProtocolFactory,
                       TProtocolFactory outputProtocolFactory) {
    super(new TProcessorFactory(processor), serverTransport,
          inputTransportFactory, outputTransportFactory,
          inputProtocolFactory, outputProtocolFactory);
  }

  public TSimpleServer(TProcessorFactory processorFactory,
          TServerTransport serverTransport) {
    super(processorFactory, serverTransport);
  }

  public TSimpleServer(TProcessorFactory processorFactory,
          TServerTransport serverTransport,
          TTransportFactory transportFactory,
          TProtocolFactory protocolFactory) {
    super(processorFactory, serverTransport, transportFactory, protocolFactory);
  }

  public TSimpleServer(TProcessorFactory processorFactory,
          TServerTransport serverTransport,
          TTransportFactory inputTransportFactory,
          TTransportFactory outputTransportFactory,
          TProtocolFactory inputProtocolFactory,
          TProtocolFactory outputProtocolFactory) {
    super(processorFactory, serverTransport,
          inputTransportFactory, outputTransportFactory,
          inputProtocolFactory, outputProtocolFactory);
  }


  public void serve() {
    stopped_ = false;
    try {
      serverTransport_.listen();
    } catch (TTransportException ttx) {
      LOGGER.error("Error occurred during listening.", ttx);
      return;
    }

    while (!stopped_) {
      TTransport client = null;
      TProcessor processor = null;
      TTransport inputTransport = null;
      TTransport outputTransport = null;
      TProtocol inputProtocol = null;
      TProtocol outputProtocol = null;
      try {
        client = serverTransport_.accept();
        if (client != null) {
          processor = processorFactory_.getProcessor(client);
          inputTransport = inputTransportFactory_.getTransport(client);
          outputTransport = outputTransportFactory_.getTransport(client);
          inputProtocol = inputProtocolFactory_.getProtocol(inputTransport);
          outputProtocol = outputProtocolFactory_.getProtocol(outputTransport);
          while (processor.process(inputProtocol, outputProtocol)) {}
        }
      } catch (TTransportException ttx) {
        // Client died, just move on
      } catch (TException tx) {
        if (!stopped_) {
          LOGGER.error("Thrift error occurred during processing of message.", tx);
        }
      } catch (Exception x) {
        if (!stopped_) {
          LOGGER.error("Error occurred during processing of message.", x);
        }
      }

      if (inputTransport != null) {
        inputTransport.close();
      }

      if (outputTransport != null) {
        outputTransport.close();
      }

    }
  }

  public void stop() {
    stopped_ = true;
    serverTransport_.interrupt();
  }
}
