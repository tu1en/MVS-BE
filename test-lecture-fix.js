const axios = require('axios');

const BASE_URL = 'http://localhost:8080';

// Test credentials (admin user)
const adminCredentials = {
    username: 'admin',
    password: 'password123'
};

let authToken = '';

async function login() {
    try {
        console.log('🔐 Logging in as admin...');
        const response = await axios.post(`${BASE_URL}/api/auth/login`, adminCredentials);
        authToken = response.data.token;
        console.log('✅ Login successful');
        return true;
    } catch (error) {
        console.error('❌ Login failed:', error.response?.data || error.message);
        return false;
    }
}

async function runDataVerification() {
    try {
        console.log('\n🔍 Running data verification...');
        const response = await axios.get(`${BASE_URL}/api/admin/data-verification/run`, {
            headers: { Authorization: `Bearer ${authToken}` }
        });
        
        const report = response.data;
        console.log('📊 Verification Results:');
        console.log(`   Total Issues: ${report.totalIssues}`);
        console.log(`   Critical Issues: ${report.criticalIssues}`);
        console.log(`   Warning Issues: ${report.warningIssues}`);
        console.log(`   Info Issues: ${report.infoIssues}`);
        
        if (report.hasIssues) {
            console.log('\n📋 Issues found:');
            report.issues.forEach(issue => {
                console.log(`   [${issue.severity}] ${issue.code}: ${issue.message}`);
                if (issue.details) {
                    console.log(`      Details: ${issue.details}`);
                }
            });
        }
        
        return report;
    } catch (error) {
        console.error('❌ Data verification failed:', error.response?.data || error.message);
        return null;
    }
}

async function fixLectureDates() {
    try {
        console.log('\n🔧 Fixing lecture dates...');
        const response = await axios.post(`${BASE_URL}/api/admin/data-fix/fix-lecture-dates`, {}, {
            headers: { Authorization: `Bearer ${authToken}` }
        });
        
        console.log('✅ Fix result:', response.data);
        return true;
    } catch (error) {
        console.error('❌ Fix lecture dates failed:', error.response?.data || error.message);
        return false;
    }
}

async function getHealthStatus() {
    try {
        console.log('\n🏥 Getting health status...');
        const response = await axios.get(`${BASE_URL}/api/admin/data-verification/health`, {
            headers: { Authorization: `Bearer ${authToken}` }
        });
        
        console.log('🏥 Health Status:', response.data);
        return response.data;
    } catch (error) {
        console.error('❌ Health check failed:', error.response?.data || error.message);
        return null;
    }
}

async function main() {
    console.log('🧪 ============== LECTURE DATE FIX TEST ==============');
    
    // Step 1: Login
    const loginSuccess = await login();
    if (!loginSuccess) {
        console.log('❌ Cannot proceed without authentication');
        return;
    }
    
    // Step 2: Run initial verification
    console.log('\n📋 STEP 1: Initial Data Verification');
    const initialReport = await runDataVerification();
    if (!initialReport) {
        console.log('❌ Cannot proceed without initial verification');
        return;
    }
    
    // Step 3: Check if there are lecture date issues
    const hasLectureDateIssues = initialReport.issues?.some(issue => 
        issue.code === 'LECTURE_NO_DATE' || issue.message.includes('lecture') || issue.message.includes('date')
    );
    
    if (!hasLectureDateIssues && initialReport.warningIssues === 0) {
        console.log('✅ No lecture date issues found. Test completed successfully!');
        return;
    }
    
    // Step 4: Fix lecture dates
    console.log('\n🔧 STEP 2: Fixing Lecture Dates');
    const fixSuccess = await fixLectureDates();
    if (!fixSuccess) {
        console.log('❌ Fix failed, cannot proceed');
        return;
    }
    
    // Step 5: Run verification again
    console.log('\n🔍 STEP 3: Post-Fix Verification');
    const postFixReport = await runDataVerification();
    if (!postFixReport) {
        console.log('❌ Post-fix verification failed');
        return;
    }
    
    // Step 6: Compare results
    console.log('\n📊 STEP 4: Results Comparison');
    console.log(`Before Fix - Total Issues: ${initialReport.totalIssues}, Warnings: ${initialReport.warningIssues}`);
    console.log(`After Fix  - Total Issues: ${postFixReport.totalIssues}, Warnings: ${postFixReport.warningIssues}`);
    
    const issuesReduced = initialReport.totalIssues - postFixReport.totalIssues;
    const warningsReduced = initialReport.warningIssues - postFixReport.warningIssues;
    
    if (issuesReduced > 0 || warningsReduced > 0) {
        console.log(`✅ SUCCESS: Reduced ${issuesReduced} total issues and ${warningsReduced} warnings`);
    } else if (postFixReport.totalIssues === 0) {
        console.log('✅ SUCCESS: All issues resolved!');
    } else {
        console.log('⚠️ No improvement detected');
    }
    
    // Step 7: Final health check
    console.log('\n🏥 STEP 5: Final Health Check');
    await getHealthStatus();
    
    console.log('\n✅ ============== TEST COMPLETED ==============');
}

// Run the test
main().catch(error => {
    console.error('💥 Test failed with error:', error);
    process.exit(1);
});
