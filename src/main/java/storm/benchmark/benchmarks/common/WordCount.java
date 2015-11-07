/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package storm.benchmark.benchmarks.common;

import static storm.benchmark.metrics.MetricsCollectorConfig.*;
import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import org.apache.log4j.Logger;

import storm.benchmark.lib.operation.WordSplit;
import storm.benchmark.metrics.system.SystemMetricCollectorBuilder;
import storm.benchmark.util.BenchmarkUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class WordCount extends StormBenchmark {
  private static final Logger LOG = Logger.getLogger(WordCount.class);

  public static final String SPOUT_ID = "spout";
  public static final String SPOUT_SITE = "spout.site";
  public static final String SPOUT_NUM = "component.spout_num";
  public static final String SPLIT_ID = "split";
  public static final String SPLIT_SITE = "split.site";
  public static final String SPLIT_NUM = "component.split_bolt_num";
  public static final String COUNT_ID = "count";
  public static final String COUNT_SITE = "count.site";
  public static final String COUNT_NUM = "component.count_bolt_num";
  public static final int DEFAULT_SPOUT_NUM = 8;
  public static final int DEFAULT_SPLIT_BOLT_NUM = 4;
  public static final int DEFAULT_COUNT_BOLT_NUM = 4;

  protected IRichSpout spout;

  @Override
  public StormTopology getTopology(Config config) {

    final int spoutNum = BenchmarkUtils.getInt(config, SPOUT_NUM, DEFAULT_SPOUT_NUM);
    final int spBoltNum = BenchmarkUtils.getInt(config, SPLIT_NUM, DEFAULT_SPLIT_BOLT_NUM);
    final int cntBoltNum = BenchmarkUtils.getInt(config, COUNT_NUM, DEFAULT_COUNT_BOLT_NUM);
    final String spoutSite = (String)config.get(SPOUT_SITE);
    final String splitSite = (String)config.get(SPLIT_SITE);
    final String countSite = (String)config.get(COUNT_SITE);

    TopologyBuilder builder = new TopologyBuilder();

    builder.setSpout(SPOUT_ID, spout, spoutNum)
    .addConfiguration("site", spoutSite);
    builder.setBolt(SPLIT_ID, new SplitSentence(), spBoltNum).localOrShuffleGrouping(
            SPOUT_ID).addConfiguration("site", splitSite);
    builder.setBolt(COUNT_ID, new Count(), cntBoltNum).fieldsGrouping(SPLIT_ID,
      new Fields(SplitSentence.FIELDS))
      	.addConfiguration("site", countSite);

    return builder.createTopology();
  }

  public IRichSpout getSpout() {
    return spout;
  }

  public static class SplitSentence extends BaseBasicBolt {

    public static final String FIELDS = "word";

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
		int duration = BenchmarkUtils.getInt(stormConf, METRICS_TOTAL_TIME, DEFAULT_TOTAL_TIME), 
			  interval = BenchmarkUtils.getInt(stormConf, METRICS_SYS_INT, DEFAULT_SYS_INT);
		LOG.info("Duration: " + duration);
		LOG.info("Interval: " + interval);
		SystemMetricCollectorBuilder
			.build(interval, duration, "/tmp/" + context.getThisComponentId() + "-" + System.currentTimeMillis() / 1000 / 1000)
			.start();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
      for (String word : WordSplit.splitSentence(input.getString(0))) {
        collector.emit(new Values(word));
      }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields(FIELDS));
    }

  }

  public static class Count extends BaseBasicBolt {
    public static final String FIELDS_WORD = "word";
    public static final String FIELDS_COUNT = "count";

    Map<String, Integer> counts = new HashMap<String, Integer>();

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
		int duration = BenchmarkUtils.getInt(stormConf, METRICS_TOTAL_TIME, DEFAULT_TOTAL_TIME), 
				  interval = BenchmarkUtils.getInt(stormConf, METRICS_SYS_INT, DEFAULT_SYS_INT);
		LOG.info("Duration: " + duration);
		LOG.info("Interval: " + interval);
		SystemMetricCollectorBuilder
			.build(interval, duration, "/tmp/" + context.getThisComponentId() + "-" + System.currentTimeMillis() / 1000 / 1000)
			.start();
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
      String word = tuple.getString(0);
      Integer count = counts.get(word);
      if (count == null)
        count = 0;
      count++;
      counts.put(word, count);
      collector.emit(new Values(word, count));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields(FIELDS_WORD, FIELDS_COUNT));
    }
  }

}
