import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Alert, Badge, Progress, Tabs, Table, Button, Space, Tag } from 'antd';
import { 
  DashboardOutlined, 
  UserOutlined, 
  SecurityScanOutlined, 
  MonitorOutlined,
  SettingOutlined,
  WarningOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined
} from '@ant-design/icons';
import { Line, Column, Pie } from '@ant-design/plots';
import moment from 'moment';
import axios from 'axios';

const { TabPane } = Tabs;

/**
 * Component dashboard quản trị hệ thống
 */
const AdminDashboard = () => {
  const [loading, setLoading] = useState(false);
  const [systemHealth, setSystemHealth] = useState(null);
  const [systemInfo, setSystemInfo] = useState(null);
  const [auditStats, setAuditStats] = useState(null);
  const [monitoringStats, setMonitoringStats] = useState(null);
  const [criticalMetrics, setCriticalMetrics] = useState([]);
  const [userActivityStats, setUserActivityStats] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');

  // Load dashboard data
  useEffect(() => {
    loadDashboardData();
    
    // Auto refresh every 30 seconds
    const interval = setInterval(loadDashboardData, 30000);
    return () => clearInterval(interval);
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      await Promise.all([
        loadSystemHealth(),
        loadSystemInfo(),
        loadAuditStatistics(),
        loadMonitoringStatistics(),
        loadCriticalMetrics(),
        loadUserActivityStatistics()
      ]);
    } catch (error) {
      console.error('Error loading dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadSystemHealth = async () => {
    try {
      const response = await axios.get('/api/admin/health');
      setSystemHealth(response.data);
    } catch (error) {
      console.error('Error loading system health:', error);
    }
  };

  const loadSystemInfo = async () => {
    try {
      const response = await axios.get('/api/admin/system-info');
      setSystemInfo(response.data);
    } catch (error) {
      console.error('Error loading system info:', error);
    }
  };

  const loadAuditStatistics = async () => {
    try {
      const response = await axios.get('/api/admin/audit-logs/statistics?days=7');
      setAuditStats(response.data);
    } catch (error) {
      console.error('Error loading audit statistics:', error);
    }
  };

  const loadMonitoringStatistics = async () => {
    try {
      const response = await axios.get('/api/admin/monitoring/statistics');
      setMonitoringStats(response.data);
    } catch (error) {
      console.error('Error loading monitoring statistics:', error);
    }
  };

  const loadCriticalMetrics = async () => {
    try {
      const response = await axios.get('/api/admin/monitoring/critical');
      setCriticalMetrics(response.data);
    } catch (error) {
      console.error('Error loading critical metrics:', error);
    }
  };

  const loadUserActivityStatistics = async () => {
    try {
      const response = await axios.get('/api/admin/users/activity-statistics?days=7');
      setUserActivityStats(response.data);
    } catch (error) {
      console.error('Error loading user activity statistics:', error);
    }
  };

  // Perform health check
  const performHealthCheck = async () => {
    try {
      setLoading(true);
      await axios.post('/api/admin/health/check');
      await loadSystemHealth();
    } catch (error) {
      console.error('Error performing health check:', error);
    } finally {
      setLoading(false);
    }
  };

  // Get health status color
  const getHealthStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'healthy':
      case 'good':
        return '#52c41a';
      case 'warning':
        return '#faad14';
      case 'critical':
      case 'error':
        return '#ff4d4f';
      default:
        return '#d9d9d9';
    }
  };

  // Get health status icon
  const getHealthStatusIcon = (status) => {
    switch (status?.toLowerCase()) {
      case 'healthy':
      case 'good':
        return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
      case 'warning':
        return <WarningOutlined style={{ color: '#faad14' }} />;
      case 'critical':
      case 'error':
        return <CloseCircleOutlined style={{ color: '#ff4d4f' }} />;
      default:
        return <SyncOutlined style={{ color: '#d9d9d9' }} />;
    }
  };

  // Format memory size
  const formatMemorySize = (bytes) => {
    if (!bytes) return 'N/A';
    const gb = bytes / (1024 * 1024 * 1024);
    return `${gb.toFixed(2)} GB`;
  };

  // Format uptime
  const formatUptime = (milliseconds) => {
    if (!milliseconds) return 'N/A';
    const days = Math.floor(milliseconds / (1000 * 60 * 60 * 24));
    const hours = Math.floor((milliseconds % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    return `${days} ngày ${hours} giờ`;
  };

  // System overview cards
  const renderSystemOverview = () => {
    return (
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="Trạng thái hệ thống"
              value={systemHealth?.overallStatus || 'Unknown'}
              prefix={getHealthStatusIcon(systemHealth?.overallStatus)}
              valueStyle={{ color: getHealthStatusColor(systemHealth?.overallStatus) }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Người dùng hoạt động"
              value={userActivityStats?.activeUsers || 0}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Cảnh báo bảo mật"
              value={auditStats?.failedLogs || 0}
              prefix={<SecurityScanOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Metrics nghiêm trọng"
              value={monitoringStats?.criticalMetrics || 0}
              prefix={<MonitorOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
      </Row>
    );
  };

  // System health details
  const renderSystemHealth = () => {
    if (!systemHealth) return null;

    return (
      <Card title="Chi tiết trạng thái hệ thống" style={{ marginBottom: 24 }}>
        <Row gutter={16}>
          <Col span={12}>
            <h4>Trạng thái các thành phần:</h4>
            {systemHealth.componentStatuses && Object.entries(systemHealth.componentStatuses).map(([component, status]) => (
              <div key={component} style={{ marginBottom: 8 }}>
                <Badge 
                  color={getHealthStatusColor(status)} 
                  text={`${component}: ${status}`} 
                />
              </div>
            ))}
          </Col>
          <Col span={12}>
            {systemHealth.issues && systemHealth.issues.length > 0 && (
              <div>
                <h4>Vấn đề cần chú ý:</h4>
                {systemHealth.issues.map((issue, index) => (
                  <Alert
                    key={index}
                    message={issue}
                    type="warning"
                    showIcon
                    style={{ marginBottom: 8 }}
                  />
                ))}
              </div>
            )}
          </Col>
        </Row>
        
        <div style={{ marginTop: 16, textAlign: 'right' }}>
          <Button 
            type="primary" 
            icon={<SyncOutlined />}
            onClick={performHealthCheck}
            loading={loading}
          >
            Kiểm tra lại
          </Button>
        </div>
      </Card>
    );
  };

  // System information
  const renderSystemInfo = () => {
    if (!systemInfo) return null;

    const memoryUsagePercent = systemInfo.totalMemory > 0 
      ? ((systemInfo.usedMemory / systemInfo.totalMemory) * 100).toFixed(1)
      : 0;

    return (
      <Card title="Thông tin hệ thống" style={{ marginBottom: 24 }}>
        <Row gutter={16}>
          <Col span={12}>
            <p><strong>Ứng dụng:</strong> {systemInfo.applicationName} v{systemInfo.version}</p>
            <p><strong>Java Version:</strong> {systemInfo.javaVersion}</p>
            <p><strong>Hệ điều hành:</strong> {systemInfo.osName} {systemInfo.osVersion}</p>
            <p><strong>CPU Cores:</strong> {systemInfo.availableProcessors}</p>
          </Col>
          <Col span={12}>
            <p><strong>Thời gian khởi động:</strong> {moment(systemInfo.startTime).format('DD/MM/YYYY HH:mm:ss')}</p>
            <p><strong>Uptime:</strong> {formatUptime(systemInfo.uptime)}</p>
            <p><strong>Bộ nhớ:</strong></p>
            <div style={{ marginLeft: 16 }}>
              <Progress 
                percent={memoryUsagePercent} 
                format={() => `${formatMemorySize(systemInfo.usedMemory)} / ${formatMemorySize(systemInfo.totalMemory)}`}
                status={memoryUsagePercent > 85 ? 'exception' : 'normal'}
              />
            </div>
          </Col>
        </Row>
      </Card>
    );
  };

  // Critical metrics table
  const renderCriticalMetrics = () => {
    const columns = [
      {
        title: 'Metric',
        dataIndex: 'metricName',
        key: 'metricName'
      },
      {
        title: 'Giá trị',
        key: 'value',
        render: (_, record) => record.getFormattedValue?.() || `${record.metricValue} ${record.metricUnit || ''}`
      },
      {
        title: 'Trạng thái',
        dataIndex: 'status',
        key: 'status',
        render: (status) => (
          <Tag color={status === 'CRITICAL' ? 'red' : status === 'WARNING' ? 'orange' : 'green'}>
            {status}
          </Tag>
        )
      },
      {
        title: 'Thời gian',
        dataIndex: 'timestamp',
        key: 'timestamp',
        render: (timestamp) => moment(timestamp).format('DD/MM HH:mm:ss')
      }
    ];

    return (
      <Card title="Metrics nghiêm trọng" style={{ marginBottom: 24 }}>
        <Table
          columns={columns}
          dataSource={criticalMetrics}
          rowKey="id"
          pagination={false}
          size="small"
        />
      </Card>
    );
  };

  // Audit statistics chart
  const renderAuditChart = () => {
    if (!auditStats?.actionCounts) return null;

    const data = Object.entries(auditStats.actionCounts).map(([action, count]) => ({
      action,
      count
    }));

    const config = {
      data,
      xField: 'action',
      yField: 'count',
      label: {
        position: 'middle',
        style: {
          fill: '#FFFFFF',
          opacity: 0.6,
        },
      },
      xAxis: {
        label: {
          autoHide: true,
          autoRotate: false,
        },
      },
      meta: {
        action: {
          alias: 'Hành động',
        },
        count: {
          alias: 'Số lượng',
        },
      },
    };

    return (
      <Card title="Thống kê hoạt động (7 ngày qua)" style={{ marginBottom: 24 }}>
        <Column {...config} />
      </Card>
    );
  };

  // User activity chart
  const renderUserActivityChart = () => {
    if (!userActivityStats?.activityByHour) return null;

    const data = Object.entries(userActivityStats.activityByHour).map(([hour, count]) => ({
      hour: `${hour}:00`,
      count
    }));

    const config = {
      data,
      xField: 'hour',
      yField: 'count',
      point: {
        size: 5,
        shape: 'diamond',
      },
      label: {
        style: {
          fill: '#aaa',
        },
      },
    };

    return (
      <Card title="Hoạt động người dùng theo giờ" style={{ marginBottom: 24 }}>
        <Line {...config} />
      </Card>
    );
  };

  return (
    <div style={{ padding: 24 }}>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2><DashboardOutlined /> Dashboard Quản trị</h2>
        <Space>
          <Button 
            icon={<SyncOutlined />} 
            onClick={loadDashboardData}
            loading={loading}
          >
            Làm mới
          </Button>
        </Space>
      </div>

      {renderSystemOverview()}

      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <TabPane tab="Tổng quan" key="overview">
          <Row gutter={16}>
            <Col span={24}>
              {renderSystemHealth()}
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              {renderSystemInfo()}
            </Col>
            <Col span={12}>
              {renderCriticalMetrics()}
            </Col>
          </Row>
        </TabPane>

        <TabPane tab="Thống kê hoạt động" key="activity">
          <Row gutter={16}>
            <Col span={12}>
              {renderAuditChart()}
            </Col>
            <Col span={12}>
              {renderUserActivityChart()}
            </Col>
          </Row>
        </TabPane>

        <TabPane tab="Giám sát hệ thống" key="monitoring">
          <Row gutter={16}>
            <Col span={24}>
              <Card title="Monitoring Dashboard">
                <Alert
                  message="Monitoring Dashboard"
                  description="Chi tiết monitoring sẽ được hiển thị ở đây với real-time metrics và alerts."
                  type="info"
                  showIcon
                />
              </Card>
            </Col>
          </Row>
        </TabPane>

        <TabPane tab="Cấu hình" key="configuration">
          <Row gutter={16}>
            <Col span={24}>
              <Card title="Cấu hình hệ thống">
                <Alert
                  message="System Configuration"
                  description="Giao diện quản lý cấu hình hệ thống sẽ được hiển thị ở đây."
                  type="info"
                  showIcon
                />
              </Card>
            </Col>
          </Row>
        </TabPane>
      </Tabs>
    </div>
  );
};

export default AdminDashboard;
