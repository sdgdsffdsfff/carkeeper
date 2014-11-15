package com.taobao.taokeeper.monitor.core;

import java.util.Timer;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.taokeeper.common.constant.SystemConstant;
import com.taobao.taokeeper.monitor.core.task.HostPerformanceCollectTask;
import com.taobao.taokeeper.monitor.core.task.ZooKeeperALiveCheckerJob;
import com.taobao.taokeeper.monitor.core.task.ZooKeeperClusterMapDumpJob;
import com.taobao.taokeeper.monitor.core.task.ZooKeeperNodeChecker;
import com.taobao.taokeeper.monitor.core.task.ZooKeeperStatusCollectJob;
import com.taobao.taokeeper.monitor.core.task.runable.ClientThroughputStatJob;
import common.toolkit.java.constant.BaseConstant;
import common.toolkit.java.util.ThreadUtil;

/**
 * Description: System Initialization
 * @author yinshi.nc
 * @Date 2011-10-27
 */
public class Initialization extends HttpServlet implements Servlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger( Initialization.class );

	public void init() {

		/** Init threadpool */
		ThreadPoolManager.init();

		// Start the job of dump db info to memeory
		Thread zooKeeperClusterMapDumpJobThread = new Thread( new ZooKeeperClusterMapDumpJob() );
		zooKeeperClusterMapDumpJobThread.start();
		try {
			// 这里等待一下，因为第一次一定要dump成功，
			// TODO 这个等待逻辑要改。
			Thread.sleep( 5000 );
		} catch ( InterruptedException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ThreadUtil.startThread( new ClientThroughputStatJob() );

		/** 启动ZooKeeper数据修改通知检测 */
		ThreadUtil.startThread( new ZooKeeperALiveCheckerJob() );

		/** 启动ZooKeeper集群状态收集 */
		ThreadUtil.startThread( new ZooKeeperStatusCollectJob() );

		/** 收集机器CPU LOAD MEMEORY */
		ThreadUtil.startThread( new HostPerformanceCollectTask() );

		Timer timer = new Timer();
		//开启ZooKeeper Node的Path检查
		timer.schedule( new ZooKeeperNodeChecker(), 5000, //
				           BaseConstant.MILLISECONDS_OF_ONE_HOUR  * 
				           SystemConstant.HOURS_RATE_OF_ZOOKEEPER_NODE_CHECK  );


		//ThreadUtil.startThread( new CheckerJob( ) );


//		ThreadUtil.startThread( new CheckerJob( "/jingwei-v2/tasks/DAILY-TMALL-DPC-META/locks" ) );


		LOG.info( "*********************************************************" );
		LOG.info( "****************TaoKeeper Startup Success****************" );
	}

}
