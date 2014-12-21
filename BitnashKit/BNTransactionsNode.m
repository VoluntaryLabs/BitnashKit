//
//  BNTransactions.m
//  BitnashKit
//
//  Created by Rich Collins on 5/2/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import "BNTransactionsNode.h"
#import "BNWallet.h"

@implementation BNTransactionsNode

- (id)init
{
    self = [super init];
    
    self.nodeTitle = @"Transactions";
    self.nodeSuggestedWidth = @500;
    self.nodeShouldUseCountForNodeNote = @YES;
    self.nodeShouldSortChildren = @YES;
    self.nodeSortChildrenKey = @"ordinality";
    
    return self;
}

- (void)fetch
{
    [self mergeWithChildren:[self.wallet transactions]];
    [self sortChildren];
}

@end
