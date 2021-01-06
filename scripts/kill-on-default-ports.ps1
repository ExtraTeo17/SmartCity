Stop-Process -Id (Get-NetTCPConnection -LocalPort 9000).OwningProcess -Force
Stop-Process -Id (Get-NetTCPConnection -LocalPort 4000).OwningProcess -Force
Stop-Process -Id (Get-NetTCPConnection -LocalPort 5000).OwningProcess -Force
