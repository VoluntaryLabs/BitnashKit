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
    self.sortChildrenKey = @"ordinality";
    
    return self;
}

- (void)fetch
{
    [self mergeWithChildren:[self.wallet transactions]];
    [self sortChildren];
}

@end
