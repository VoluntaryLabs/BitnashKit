//
//  BNTransactions.m
//  BitnashKit
//
//  Created by Rich Collins on 5/2/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNTransactionsNode.h"
#import "BNWallet.h"

@implementation BNTransactionsNode

- (id)init
{
    self = [super init];
    
    self.nodeTitle = @"Transactions";
    self.nodeSuggestedWidth = 500;
    self.shouldUseCountForNodeNote = YES;
    self.shouldSortChildren = YES;
    self.sortChildrenKey = @"confirmations"; //@"updateTime";
    
    return self;
}

- (void)fetch
{
    [self mergeWithChildren:[self.wallet transactions]];
    for (BNTx *tx in self.children)
    {
        if ([tx.description containsString:@"Zeroconf"])
        {
            NSLog(@"%@, %@", tx.txHash, tx.description);
        }
    }
    [self sortChildren];
}

@end
